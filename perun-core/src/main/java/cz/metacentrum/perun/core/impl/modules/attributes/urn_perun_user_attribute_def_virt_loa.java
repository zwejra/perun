package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserExtSourceAddedToUser;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserExtSourceRemovedFromUser;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserExtSourceUpdated;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Module for user virtual attribute loa
 *
 * This module return the highest value from user's UserExtSources LoAs. If the attribute value is null throw WrongAttributeValueException.
 *
 * @author Pavel Vyskocil vyskocilpavel@muni.cz
 */
public class urn_perun_user_attribute_def_virt_loa extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private static final String A_U_V_LOA = AttributesManager.NS_USER_ATTR_VIRT + ":" + "loa";

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, User user, Attribute attribute) throws WrongAttributeValueException {
		if(attribute.getValue() == null) throw new WrongAttributeValueException(attribute, user, "Attribute value is null.");
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {

		List<UserExtSource> extSources = sess.getPerunBl().getUsersManagerBl().getActiveUserExtSources(sess, user);
		Integer maxLoa = 0;
		for(UserExtSource e : extSources) {
			if(maxLoa < e.getLoa()) maxLoa = e.getLoa();
		}
		Attribute attribute = new Attribute(attributeDefinition);
		attribute.setValue(maxLoa.toString());
		return attribute;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("loa");
		attr.setDisplayName("Level of assurance");
		attr.setType(String.class.getName());
		attr.setDescription("The highest value of LoA from all user's userExtSources.");
		return attr;
	}

	@Override
	public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl sess, AuditEvent message) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {

		List<AuditEvent> resolvingMessages = new ArrayList<>();
		if (message == null) return resolvingMessages;

		if (message instanceof UserExtSourceAddedToUser) {
			resolvingMessages.add(resolveEvent(sess, ((UserExtSourceAddedToUser) message).getUser()));
		} else if (message instanceof UserExtSourceRemovedFromUser) {
			resolvingMessages.add(resolveEvent(sess, ((UserExtSourceRemovedFromUser) message).getUser()));
		} else if (message instanceof UserExtSourceUpdated) {
			try {
				resolvingMessages.add(resolveEvent(sess, sess.getPerunBl().getUsersManagerBl().getUserById(
						sess, ((UserExtSourceUpdated) message).getUserExtSource().getUserId())));
			} catch (UserNotExistsException e) {
				throw new ConsistencyErrorException("User associated with updated UserExtSource no longer exists while resolving virtual attribute value change.", e);
			}
		}
		return resolvingMessages;

	}

	/**
	 * Resolve and create new auditer message about LOA attribute change based on current attribute value.
	 *
	 * @param sess PerunSession
	 * @param user User to resolve LoA messages
	 * @return List of new messages or empty list
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private AuditEvent resolveEvent(PerunSessionImpl sess, User user) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {

		Attribute attribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_U_V_LOA);

		if (attribute.getValue() == null) {
			return new AttributeRemovedForUser(new AttributeDefinition(attribute),user);
		} else {
			return new AttributeSetForUser(attribute,user);
		}

	}

}
