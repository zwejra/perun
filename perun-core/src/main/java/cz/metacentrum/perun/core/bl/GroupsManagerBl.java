package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichGroup;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupMoveNotAllowedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationAlreadyExists;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationCannotBeRemoved;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationDoesNotExist;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationNotAllowed;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.GroupStructureSynchronizationAlreadyRunningException;
import cz.metacentrum.perun.core.api.exceptions.GroupSynchronizationAlreadyRunningException;
import cz.metacentrum.perun.core.api.exceptions.GroupSynchronizationNotEnabledException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberGroupMismatchException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;

import java.util.List;
import java.util.Map;

/**
 * <p>Groups manager can do all work about groups in VOs.</p>
 *
 * <p>You must get an instance of GroupsManager from instance of Perun (perun si singleton - see how to get it's instance on wiki):</p>
 * <pre>
 *    GroupsManager gm = perun.getGroupsManager();
 * </pre>
 *
 * @author  Michal Prochazka
 * @author  Slavek Licehammer
 * @see Perun
 */
public interface GroupsManagerBl {


	/**
	 * Creates a new top-level group and associates it with the VO from parameter.
	 *
	 * For this method the new group has always same shortName like Name.
	 * Important: voId in object group is ignored
	 *
	 * @param perunSession
	 * @param vo to associates group with
	 * @param group new group with name without ":"
	 *
	 * @return newly created top-level group
	 *
	 * @throws InternalErrorException if group.name contains ':' or other internal error occur
	 * @throws GroupExistsException
	 */
	Group createGroup(PerunSession perunSession, Vo vo, Group group) throws GroupExistsException;

	/**
	 * Creates a new subgroup of the existing group.
	 *
	 * @param perunSession
	 * @param parentGroup
	 * @param group group.name must contain only shortName (without ":"). Hierarchy is defined by parentGroup parameter.
	 *
	 * @return newly created sub group with full group.Name with ":"
	 * @throws GroupExistsException
	 * @throws InternalErrorException
	 * @throws GroupRelationNotAllowed
	 * @throws GroupRelationAlreadyExists
	 */
	Group createGroup(PerunSession perunSession, Group parentGroup, Group group) throws GroupExistsException, GroupRelationNotAllowed, GroupRelationAlreadyExists;

	/**
	 * Gets all groups which have enabled synchronization.
	 *
	 * @param sess
	 * @return list of groups to synchronize
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 */
	List<Group> getGroupsToSynchronize(PerunSession sess);

	/**
	 * If forceDelete is false, delete only group and if this group has members or subgroups, throw an exception.
	 * If forceDelete is true, delete group with all subgroups, members and administrators, then delete this group.
	 *
	 * @param perunSession
	 * @param group group to delete
	 * @param forceDelete if forceDelete is false, delete group only if is empty and has no subgroups, if is true, delete anyway with all connections
	 *
	 * @throws InternalErrorException
	 * @throws RelationExistsException
	 * @throws GroupAlreadyRemovedException
	 * @throws GroupAlreadyRemovedFromResourceException
	 * @throws GroupNotExistsException
	 * @throws GroupRelationDoesNotExist
	 * @throws GroupRelationCannotBeRemoved
	 */
	void deleteGroup(PerunSession perunSession, Group group, boolean forceDelete) throws RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved;

	/**
	 * Delete all groups in list from perun. (Except members group)
	 *
	 * If forceDelete is false, delete groups only if none of them (IN MOMENT OF DELETING) has subgroups and members, in other case throw exception.
	 * if forceDelete is true, delete groups with all subgroups and members.
	 *
	 * Groups are deleted in order: from longest name to the shortest
	 *	- ex: Group A:b:c will be deleted sooner than Group A:b etc.
	 *	- reason for this: with group are deleted its subgroups too
	 *
	 * Important: Groups can be from different VOs.
	 *
	 * @param perunSession
	 * @param groups list of groups to deleted
	 * @param forceDelete if forceDelete is false, delete groups only if all of them have no subgroups and no members, if is true, delete anyway with all connections
	 *
	 * @throws InternalErrorException
	 * @throws GroupAlreadyRemovedException
	 * @throws RelationExistsException
	 * @throws GroupAlreadyRemovedFromResourceException
	 * @throws GroupNotExistsException
	 * @throws GroupRelationDoesNotExist
	 * @throws GroupRelationCannotBeRemoved
	 */
	void deleteGroups(PerunSession perunSession, List<Group> groups, boolean forceDelete) throws GroupAlreadyRemovedException, RelationExistsException, GroupAlreadyRemovedFromResourceException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved;

	/**
	 * Deletes built-in members group.
	 *
	 * @param sess
	 * @param vo
	 * @throws InternalErrorException
	 * @throws GroupAlreadyRemovedException
	 * @throws GroupAlreadyRemovedFromResourceException
	 * @throws GroupNotExistsException
	 * @throws GroupRelationDoesNotExist
	 * @throws GroupRelationCannotBeRemoved
	 */
	void deleteMembersGroup(PerunSession sess, Vo vo) throws GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved;

	/**
	 * Deletes all groups under the VO except built-in groups (members, admins groups).
	 *
	 * @param perunSession
	 * @param vo VO
	 *
	 * @throws InternalErrorException
	 * @throws GroupAlreadyRemovedException
	 * @throws GroupAlreadyRemovedFromResourceException
	 * @throws GroupRelationDoesNotExist
	 * @throws GroupRelationCannotBeRemoved
	 */
	void deleteAllGroups(PerunSession perunSession, Vo vo) throws GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved;

	/**
	 * Updates group by ID.
	 *
	 * Update shortName (use shortName) and description. Group.name is ignored.
	 * Return Group with correctly set parameters (including group.name)
	 *
	 * @param perunSession
	 * @param group to update (use only ID, shortName and description)
	 *
	 * @return updated group with correctly set parameters (including group.name)
	 *
	 * @throws InternalErrorException
	 * @throws GroupExistsException if group with same name already exists in the same VO
	 */
	Group updateGroup(PerunSession perunSession, Group group) throws GroupExistsException;

	/**
	 * Updates parentGroupId.
	 *
	 * !! IMPORTANT This method allows to change parentGroupId, but it doesn't update group and subGroup names !!
	 *
	 * @param perunSession
	 * @param group to update
	 *
	 * @return group with updated parentGroupId
	 *
	 * @throws InternalErrorException
	 */
	Group updateParentGroupId(PerunSession perunSession, Group group);

	/**
	 * Search for the group with specified id in all VOs.
	 *
	 * @param id
	 * @param perunSession
	 *
	 * @return group with specified id or throws
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 */
	Group getGroupById(PerunSession perunSession, int id) throws GroupNotExistsException;

	/**
	 * Search for the group with specified name in specified VO.
	 *
	 * IMPORTANT: need to use full name of group (ex. 'toplevel:a:b', not the shortname which is in this example 'b')
	 *
	 * @param perunSession
	 * @param vo
	 * @param name
	 *
	 * @return group with specified name or throws   in specified VO
	 *
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 */
	Group getGroupByName(PerunSession perunSession, Vo vo, String name) throws GroupNotExistsException;

	/**
	 * Search for the groups with specified ids in all VOs.
	 *
	 * @param ids
	 * @param perunSession
	 *
	 * @return groups with specified ids
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsByIds(PerunSession perunSession, List<Integer> ids);


	/**
	 * Adds member of the VO to the group in the same VO. But not to administrators and members group.
	 *
	 * @param perunSession
	 * @param group
	 * @param member
	 * @throws InternalErrorException
	 * @throws AlreadyMemberException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws GroupNotExistsException
	 */
	void addMember(PerunSession perunSession, Group group,  Member member) throws AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupNotExistsException;

	/**
	 * Return true if Member is direct member of the Group
	 *
	 *
	 * @param sess session
	 * @param group group where the membership is to be checked
	 * @param member member whose membership is to be checked
	 * @return true if Member is direct member of the Group
	 *
	 * @throws InternalErrorException
	 */
	boolean isDirectGroupMember(PerunSession sess, Group group, Member member);



	/**
	 * Adds member of the VO to the groups in the same VO. But not to administrators and members group.
	 *
	 * @param perunSession
	 * @param groups
	 * @param member
	 * @throws InternalErrorException
	 * @throws AlreadyMemberException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws GroupNotExistsException
	 */
	void addMember(PerunSession perunSession, List<Group> groups,  Member member) throws AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupNotExistsException;

	/**
	 * Adds members of the VO to the group in the same VO. But not to administrators and members group.
	 *
	 * @param perunSession
	 * @param group
	 * @param members
	 * @throws InternalErrorException
	 * @throws AlreadyMemberException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws GroupNotExistsException
	 */
	void addMembers(PerunSession perunSession, Group group,  List<Member> members) throws AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupNotExistsException;


	/**
	 * Special addMember which is able to add members into the members and administrators group.
	 *
	 * @param perunSession
	 * @param group
	 * @param member
	 * @throws InternalErrorException
	 * @throws AlreadyMemberException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws GroupNotExistsException
	 */
	void addMemberToMembersGroup(PerunSession perunSession, Group group,  Member member) throws AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupNotExistsException;

	/**
	 * Return list of assigned groups on the resource (without subgroups unless they are assigned too)
	 *
	 * @param perunSession
	 * @param resource
	 *
	 * @return list of groups, which are assigned on the resource
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAssignedGroupsToResource(PerunSession perunSession, Resource resource);

	/**
	 * Return list of assigned groups on the resource (without subgroups unless they are assigned too),
	 * which contain specific member
	 *
	 * @param perunSession
	 * @param resource
	 * @param member
	 *
	 * @return list of groups, which are assigned on the resource and contain specific member
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAssignedGroupsToResource(PerunSession perunSession, Resource resource, Member member);

	/** Return list of assigned groups on the resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @param withSubGroups if true returns also all subgroups of assigned groups
	 *
	 * @return list of groups, which are assigned on the resource
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAssignedGroupsToResource(PerunSession perunSession, Resource resource, boolean withSubGroups);

	/** Return list of assigned groups on all facility resources (without subgroups unless they are assigned too)
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @return list of groups, which are assigned on all facility resources
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAssignedGroupsToFacility(PerunSession perunSession, Facility facility);

	/**
	 * Removes member form the group. But not from members or administrators group.
	 *
	 * @param perunSession
	 * @param group
	 * @param member
	 *
	 * @throws InternalErrorException
	 * @throws NotGroupMemberException
	 * @throws GroupNotExistsException
	 */
	void removeMember(PerunSession perunSession, Group group, Member member) throws NotGroupMemberException, GroupNotExistsException;

	/**
	 * Removes members from the group. But not from members or administrators group.
	 *
	 * @param perunSession
	 * @param group
	 * @param members
	 *
	 * @throws InternalErrorException
	 * @throws NotGroupMemberException
	 * @throws GroupNotExistsException
	 */
	void removeMembers(PerunSession perunSession, Group group, List<Member> members) throws NotGroupMemberException, GroupNotExistsException;

	/**
	 * Removes member from groups. But not from members or administrators group.
	 *
	 * @param perunSession
	 * @param groups
	 * @param member
	 *
	 * @throws InternalErrorException
	 * @throws NotGroupMemberException
	 * @throws GroupNotExistsException
	 */
	void removeMember(PerunSession perunSession, List<Group> groups, Member member) throws NotGroupMemberException, GroupNotExistsException;



	/**
	 * Removes member from members or administrators group only.
	 *
	 * @param perunSession
	 * @param group
	 * @param member
	 * @throws InternalErrorException
	 * @throws NotGroupMemberException
	 * @throws GroupNotExistsException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	void removeMemberFromMembersOrAdministratorsGroup(PerunSession perunSession, Group group, Member member) throws NotGroupMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Return all group members.
	 *
	 * @param perunSession
	 * @param group
	 * @return list of users or empty list if the group is empty
	 *
	 * @throws InternalErrorException
	 */
	List<Member> getGroupMembers(PerunSession perunSession, Group group);

	/**
	 * Get group member by member ID.
	 *
	 * @param sess
	 * @param group
	 * @param memberId
	 * @return Member
	 * @throws InternalErrorException
	 * @throws NotGroupMemberException
	 */
	Member getGroupMemberById(PerunSession sess, Group group, int memberId) throws NotGroupMemberException;

	/**
	 * Return all direct group members.
	 *
	 * @param perunSession perun session
	 * @param group group
	 * @return list of direct members
	 * @throws InternalErrorException internal error
	 */
	List<Member> getGroupDirectMembers(PerunSession perunSession, Group group);

	/**
	 * Return all members of the group who are active (valid) in the group.
	 *
	 * Do not return expired members of the group.
	 *
	 * @param sess perun session
	 * @param group to get members from
	 * @return list of active (valid) members
	 * @throws InternalErrorException
	 */
	List<Member> getActiveGroupMembers(PerunSession perunSession, Group group);

	/**
	 * Return all members of the group who are active (valid) in the group and have specific status in the Vo.
	 *
	 * Do not return expired members of the group.
	 *
	 * @param sess perun session
	 * @param group to get members from
	 * @param status to get only members with this specific status in the Vo
	 * @return list of active (valid) members with specific status in the Vo
	 * @throws InternalErrorException
	 */
	List<Member> getActiveGroupMembers(PerunSession sess, Group group, Status status);

	/**
	 * Return all members of the group who are inactive (expired) in the group.
	 *
	 * Do not return active members of the group.
	 *
	 * @param sess perun session
	 * @param group to get members from
	 * @return list of inactive (expired) members
	 * @throws InternalErrorException
	 */
	List<Member> getInactiveGroupMembers(PerunSession perunSession, Group group);

	/**
	 * Return all members of the group who are inactive (expired) in the group and have specific status in the Vo.
	 *
	 * Do not return active members of the group.
	 *
	 * @param sess perun session
	 * @param group to get members from
	 * @param status to get only members with this specific status in the Vo
	 * @return list of inactive (expired) members with specific status in the Vo
	 * @throws InternalErrorException
	 */
	List<Member> getInactiveGroupMembers(PerunSession sess, Group group, Status status);

	/**
	 * Return all members of the group who has specific status in the group and also specific status in the Vo.
	 *
	 * For example: All members with valid status in the Vo and also valid status in the group.
	 *
	 * @param sess perun session
	 * @param group to get members from
	 * @param statusInGroup to get only members with this specific status in the group
	 * @param status to get only members with this specific status in the Vo
	 * @return list of members with specific status in group and specific status in the Vo
	 * @throws InternalErrorException
	 */
	List<Member> getGroupMembers(PerunSession sess, Group group, MemberGroupStatus statusInGroup, Status status);

	/**
	 * Return only valid, suspended, expired and disabled group members.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @return list members or empty list if there are no such members
	 *
	 * @throws InternalErrorException
	 */
	List<Member> getGroupMembersExceptInvalid(PerunSession perunSession, Group group);

	/**
	 * Return only valid, suspended and expired group members.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @return list members or empty list if there are no such members
	 *
	 * @throws InternalErrorException
	 */
	List<Member> getGroupMembersExceptInvalidAndDisabled(PerunSession perunSession, Group group);

	/**
	 * Return group members.
	 *
	 * @param perunSession
	 * @param group
	 * @param status
	 *
	 * @return list users or empty list if there are no users on specified page
	 *
	 * @throws InternalErrorException
	 */
	List<Member> getGroupMembers(PerunSession perunSession, Group group, Status status);

	/**
	 * Return group users sorted by name.
	 *
	 * @param perunSession
	 * @param group
	 * @return list users sorted or empty list if there are no users on specified page
	 */
	List<User> getGroupUsers(PerunSession perunSession, Group group);

	/**
	 * Returns group members in the RichMember object, which contains Member+User data.
	 *
	 * @param sess
	 * @param group
	 *
	 * @return list of RichMembers
	 * @throws InternalErrorException
	 */
	List<RichMember> getGroupRichMembers(PerunSession sess, Group group);

	/**
	 * Returns direct group members in the RichMember object, which contains Member+User data.
	 *
	 * @param sess session
	 * @param group group
	 * @return list of direct RichMembers
	 * @throws InternalErrorException internal error
	 */
	List<RichMember> getGroupDirectRichMembers(PerunSession sess, Group group);

	/**
	 * Returns only valid, suspended and expired group members in the RichMember object, which contains Member+User data.
	 *
	 * @param sess
	 * @param group
	 *
	 * @return list of RichMembers
	 * @throws InternalErrorException
	 */
	List<RichMember> getGroupRichMembersExceptInvalid(PerunSession sess, Group group);

	/**
	 * Returns group members in the RichMember object, which contains Member+User data.
	 *
	 * @param sess
	 * @param group
	 * @param status
	 *
	 * @return list of RichMembers
	 * @throws InternalErrorException
	 */
	List<RichMember> getGroupRichMembers(PerunSession sess, Group group, Status status);

	/**
	 * Returns group members in the RichMember object, which contains Member+User data. Also contains user and member attributes.
	 *
	 * @param sess
	 * @param group
	 *
	 * @return list of RichMembers
	 * @throws InternalErrorException
	 */
	List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group);

	/**
	 * Returns only valid, suspended and expired group members in the RichMember object, which contains Member+User data. Also contains user and member attributes.
	 *
	 * @param sess
	 * @param group
	 *
	 * @return list of RichMembers
	 * @throws InternalErrorException
	 */
	List<RichMember> getGroupRichMembersWithAttributesExceptInvalid(PerunSession sess, Group group);

	/**
	 * Returns group members in the RichMember object, which contains Member+User data. Also contains user and member attributes.
	 *
	 * @param sess
	 * @param group
	 * @param status
	 *
	 * @return list of RichMembers
	 * @throws InternalErrorException
	 */
	List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group, Status status);

	/**
	 * @param perunSession
	 * @param group
	 *
	 * @return count of members of specified group
	 *
	 * @throws InternalErrorException
	 */
	int getGroupMembersCount(PerunSession perunSession, Group group);

	/**
	 * Returns counts of group members by their status in VO.
	 *
	 * @param sess
	 * @param group
	 * @return map of member status in VO to count of group members with the status
	 */
	Map<Status, Integer> getGroupMembersCountsByVoStatus(PerunSession sess, Group group);

	/**
	 * Returns counts of group members by their group status.
	 *
	 * @param sess
	 * @param group
	 * @return map of member status in group to count of group members with the status
	 */
	Map<MemberGroupStatus, Integer> getGroupMembersCountsByGroupStatus(PerunSession sess, Group group);

	/**
	 * Checks whether the user is member of the group.
	 *
	 * @param sess
	 * @param user
	 * @param group
	 * @return true if the user is member of the group
	 * @throws InternalErrorException
	 */
	boolean isUserMemberOfGroup(PerunSession sess, User user, Group group);

	/**
	 * Get all groups of the VO.
	 *
	 * @param sess
	 * @param vo
	 *
	 * @return list of groups
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAllGroups(PerunSession sess, Vo vo);

	/**
	 * Get all groups of the VO stored in the map reflecting the hierarchy.
	 *
	 * @param sess
	 * @param vo
	 *
	 * @return map of the groups hierarchically organized
	 *
	 * @throws InternalErrorException
	 */
	Map<Group, Object> getAllGroupsWithHierarchy(PerunSession sess, Vo vo);

	/**
	 * Get parent group.
	 * If group is topLevel group or Members group, return Members group.
	 *
	 * @param sess
	 * @param group
	 * @return parent group
	 * @throws InternalErrorException
	 * @throws ParentGroupNotExistsException
	 */
	Group getParentGroup(PerunSession sess, Group group) throws ParentGroupNotExistsException;

	/**
	 * Get all subgroups of the parent group under the VO.
	 *
	 * @param sess
	 * @param parentGroup parent group
	 *
	 * @return list of groups
	 * @throws InternalErrorException
	 */
	List<Group> getSubGroups(PerunSession sess, Group parentGroup);

	/**
	 * Get all subgroups of the parentGroup recursively.
	 * (parentGroup subgroups, their subgroups etc...)
	 *
	 * @param sess
	 * @param parentGroup parent group
	 *
	 * @return list of groups
	 * @throws InternalErrorException
	 */
	List<Group> getAllSubGroups(PerunSession sess, Group parentGroup);

	/**
	 * Get list of all user administrators for supported role and specific group.
	 *
	 * If onlyDirectAdmins is true, return only direct users of the group for supported role.
	 *
	 * Supported roles: GroupAdmin
	 *
	 * @param perunSession
	 * @param group
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 *
	 * @return list of all user administrators of the given group for supported role
	 *
	 * @throws InternalErrorException
	 */
	List<User> getAdmins(PerunSession perunSession, Group group, boolean onlyDirectAdmins);

	/**
	 * Get list of all richUser administrators for the group and supported role with specific attributes.
	 *
	 * Supported roles: GroupAdmin
	 *
	 * If "onlyDirectAdmins" is "true", return only direct users of the group for supported role with specific attributes.
	 * If "allUserAttributes" is "true", do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @param specificAttributes list of specified attributes which are needed in object richUser
	 * @param allUserAttributes if true, get all possible user attributes and ignore list of specificAttributes (if false, get only specific attributes)
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 *
	 * @return list of RichUser administrators for the group and supported role with attributes
	 *
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichAdmins(PerunSession perunSession, Group group, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws UserNotExistsException;

	/**
	 * Gets list of all user administrators of this group.
	 * If some group is administrator of the given group, all members are included in the list.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @throws InternalErrorException
	 *
	 * @return list of administrators
	 */
	@Deprecated
	List<User> getAdmins(PerunSession perunSession, Group group);

	/**
	 * Gets list of direct user administrators of this group.
	 * 'Direct' means, there aren't included users, who are members of group administrators, in the returned list.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @throws InternalErrorException
	 *
	 * @return list of direct administrators
	 */
	@Deprecated
	List<User> getDirectAdmins(PerunSession perunSession, Group group);

	/**
	 * Gets list of all group administrators of this group.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @throws InternalErrorException
	 *
	 * @return list of group administrators
	 */
	List<Group> getAdminGroups(PerunSession perunSession, Group group);

	/**
	 * Gets list of all administrators of this group like RichUsers without attributes.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @throws InternalErrorException
	 */
	@Deprecated
	List<RichUser> getRichAdmins(PerunSession perunSession, Group group);

	/**
	 * Gets list of all administrators of this group, which are assigned directly, like RichUsers without attributes.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @throws InternalErrorException
	 */
	@Deprecated
	List<RichUser> getDirectRichAdmins(PerunSession perunSession, Group group);

	/**
	 * Gets list of all administrators of this group like RichUsers with attributes.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	@Deprecated
	List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Group group) throws UserNotExistsException;

	/**
	 * Get list of Group administrators with specific attributes.
	 * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
	 *
	 * @param perunSession
	 * @param group
	 * @param specificAttributes
	 * @return list of RichUsers with specific attributes.
	 * @throws InternalErrorException
	 */
	@Deprecated
	List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes);

	/**
	 * Get list of Group administrators, which are directly assigned (not by group membership) with specific attributes.
	 * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
	 *
	 * @param perunSession
	 * @param group
	 * @param specificAttributes
	 * @return list of RichUsers with specific attributes.
	 * @throws InternalErrorException
	 */
	@Deprecated
	List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes);

	/**
	 * Get all groups of users under the VO.
	 *
	 * @param sess
	 * @param vo vo
	 *
	 * @throws InternalErrorException
	 *
	 * @return list of groups
	 */
	List<Group> getGroups(PerunSession sess, Vo vo);

	/**
	 * @param sess
	 * @param vo
	 *
	 * @return count of VO's groups
	 *
	 * @throws InternalErrorException
	 */
	int getGroupsCount(PerunSession sess, Vo vo);

	/**
	 * Get count of all groups.
	 *
	 * @param perunSession
	 *
	 * @return count of all groups
	 *
	 * @throws InternalErrorException
	 */
	int getGroupsCount(PerunSession perunSession);

	/**
	 * Returns number of immediate subgroups of the parent group.
	 *
	 * @param sess
	 * @param parentGroup
	 *
	 * @return count of parent group immediate subgroups
	 *
	 * @throws InternalErrorException
	 */
	int getSubGroupsCount(PerunSession sess, Group parentGroup);

	/**
	 * Gets the Vo which is owner of the group.
	 *
	 * @param sess
	 * @param group
	 *
	 * @return Vo which is owner of the group.
	 *
	 * @throws InternalErrorException
	 */
	Vo getVo(PerunSession sess, Group group);

	/**
	 * Get members from parent group. If the parent group doesn't exist (this is top level group) return all VO (from which the group is) members instead.
	 *
	 * @param sess
	 * @param group
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	List<Member> getParentGroupMembers(PerunSession sess, Group group);

	/**
	 * Get members form the parent group in RichMember format.
	 * @param sess
	 * @param group
	 * @return list of parent group rich members
	 * @throws InternalErrorException
	 */
	List<RichMember> getParentGroupRichMembers(PerunSession sess, Group group);

	/**
	 * Get members form the parent group in RichMember format including user/member attributes.
	 * @param sess
	 * @param group
	 * @return list of parent group rich members
	 * @throws InternalErrorException
	 */
	List<RichMember> getParentGroupRichMembersWithAttributes(PerunSession sess, Group group);

	/**
	 * Synchronizes the group with the external group without checking if the synchronization is already in progress.
	 * If some members from extSource of this group were skipped, return info about them.
	 * if not, return empty string instead, which means all members was successfully load from extSource.
	 *
	 * @param sess
	 * @param group
	 * @return List of strings with skipped users with reasons why were skipped
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws ExtSourceNotExistsException
	 * @throws GroupNotExistsException
	 */
	List<String> synchronizeGroup(PerunSession sess, Group group) throws AttributeNotExistsException, WrongAttributeAssignmentException, ExtSourceNotExistsException, GroupNotExistsException;

	/**
	 * Synchronize the group with external group. It checks if the synchronization of the same group is already in progress.
	 *
	 * @param sess
	 * @param group
	 * @throws InternalErrorException
	 * @throws GroupSynchronizationAlreadyRunningException when synchronization for the group is already running
	 * @throws GroupSynchronizationNotEnabledException when group doesn't have synchronization enabled
	 */
	void forceGroupSynchronization(PerunSession sess, Group group) throws GroupSynchronizationAlreadyRunningException, GroupSynchronizationNotEnabledException;

	/**
	 * Synchronize the group structure with an external group structure. It checks if the synchronization of the same group is already in progress.
	 *
	 * @param group the group to be forced this way
	 * @throws InternalErrorException
	 * @throws GroupStructureSynchronizationAlreadyRunningException
	 */
	void forceGroupStructureSynchronization(PerunSession sess, Group group) throws GroupStructureSynchronizationAlreadyRunningException;

	/**
	 * Force synchronization for all subgroups (recursively - whole tree) of the group (useful for group structure)
	 *
	 * @param sess
	 * @param group the group where all its subgroups will be forced to synchronize
	 */
	void forceAllSubGroupsSynchronization(PerunSession sess, Group group);

	/**
	 * Synchronize all groups which have enabled synchronization. This method is run by the scheduler every 5 minutes.
	 *
	 * @throws InternalErrorException
	 */
	void synchronizeGroups(PerunSession sess);

	/**
	 * Synchronize all groups structures which have enabled group structure synchronization. This method is run by the scheduler every 5 minutes.
	 *
	 * @throws InternalErrorException
	 */
	void synchronizeGroupsStructures(PerunSession sess);

	/**
	 * Returns all members groups. Except 'members' group.
	 *
	 * @param sess
	 * @param member
	 * @return
	 * @throws InternalErrorException
	 */
	List<Group> getMemberGroups(PerunSession sess, Member member);

	/**
	 * Get all groups (except member groups) where member has direct membership.
	 *
	 * @param sess
	 * @param member to get information about
	 * @return list of groups where member is direct member (not members group), empty list if there is no such group
	 * @throws InternalErrorException
	 */
	List<Group> getMemberDirectGroups(PerunSession sess, Member member);

	/**
	 * Method return list of groups for selected member which (groups) has set specific attribute.
	 * Attribute can be only from namespace "GROUP"
	 *
	 * @param sess sess
	 * @param member member
	 * @param attribute attribute from "GROUP" namespace
	 *
	 * @return list of groups which contain member and have attribute with same value
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Group> getMemberGroupsByAttribute(PerunSession sess, Member member, Attribute attribute) throws WrongAttributeAssignmentException;

	/**
	 * Return all member's groups. Included members and administrators groups.
	 *
	 * @param sess
	 * @param member
	 * @return
	 * @throws InternalErrorException
	 */
	List<Group> getAllMemberGroups(PerunSession sess, Member member);

	/**
	 * Returns all member's groups where member is in active state (is valid there)
	 * Excluded members group.
	 *
	 * @param sess perun session
	 * @param member member to get groups for
	 * @return list of groups where member is in active state (valid)
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsWhereMemberIsActive(PerunSession sess, Member member);

	/**
	 * Returns all member's groups where member is in inactive state (it is not valid and it is expired there)
	 * Excluded members group.
	 *
	 * @param sess perun session
	 * @param member member to get groups for
	 * @return list of groups where member is in inactive state (expired)
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsWhereMemberIsInactive(PerunSession sess, Member member);

	/**
	 * Returns all member's groups where member is in active state (is valid there)
	 * Included members group.
	 *
	 * @param sess perun session
	 * @param member member to get groups for
	 * @return list of groups where member is in active state (valid)
	 * @throws InternalErrorException
	 */
	List<Group> getAllGroupsWhereMemberIsActive(PerunSession sess, Member member);


	/**
	 * Returns all groups which have set the attribute with the value. Searching only def and opt attributes.
	 *
	 * @param sess
	 * @param attribute
	 * @return list of groups
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Group> getGroupsByAttribute(PerunSession sess, Attribute attribute) throws WrongAttributeAssignmentException;

	/**
	 * Returns all group-resource which have set the attribute with the value. Searching only def and opt attributes.
	 *
	 * @param sess
	 * @param attribute
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Pair<Group, Resource>> getGroupResourcePairsByAttribute(PerunSession sess, Attribute attribute) throws WrongAttributeAssignmentException;

	/**
	 * Return true if Member is member of the Group
	 *
	 * @param sess
	 * @param group
	 * @param member
	 * @return true if Member is member of the Group
	 *
	 * @throws InternalErrorException
	 */
	boolean isGroupMember(PerunSession sess, Group group, Member member);

	/**
	 * Returns list of groups connected with a member
	 *
	 * @param sess
	 * @param member
	 * @return list of groups connected with member
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsByPerunBean(PerunSession sess, Member member);

	/**
	 * Returns list of groups connected with a resource
	 *
	 * @param sess
	 * @param resource
	 * @return list of groups connected with resource
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsByPerunBean(PerunSession sess, Resource resource);

	/**
	 * Returns list of groups connected with a user
	 *
	 * @param sess
	 * @param user
	 * @return list of groups connected with user
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsByPerunBean(PerunSession sess, User user);

	/**
	 * Returns list of groups connected with a host
	 *
	 * @param sess
	 * @param host
	 * @return list of groups connected with host
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsByPerunBean(PerunSession sess, Host host);

	/**
	 * Returns list of groups connected with a facility
	 *
	 * @param sess
	 * @param facility
	 * @return list of groups connected with facility
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsByPerunBean(PerunSession sess, Facility facility);

	/**
	 * Returns list of groups connected with a vo
	 *
	 * @param sess
	 * @param vo
	 * @return list of groups connected with vo
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsByPerunBean(PerunSession sess, Vo vo);

	void checkGroupExists(PerunSession sess, Group group) throws GroupNotExistsException;

	/**
	 * This method take list of members (also with duplicit) and:
	 * 1] add all members with direct membership to target list
	 * 2] add all members with indirect membership who are not already in target list to the target list
	 *
	 * @param members list of members to filtering
	 * @return filteredMembers list of members without duplicit after filtering
	 */
	List<Member> filterMembersByMembershipTypeInGroup(List<Member> members);

	/**
	 * For richGroup filter all his group attributes and remove all which principal has no access to.
	 *
	 * @param sess
	 * @param richGroup
	 * @return richGroup with only allowed attributes
	 * @throws InternalErrorException
	 */
	RichGroup filterOnlyAllowedAttributes(PerunSession sess, RichGroup richGroup);

	/**
	 * For list of richGroups filter all their group attributes and remove all which principal has no access to.
	 *
	 * @param sess
	 * @param richGroups
	 * @return list of RichGroups with only allowed attributes
	 * @throws InternalErrorException
	 */
	List<RichGroup> filterOnlyAllowedAttributes(PerunSession sess, List<RichGroup> richGroups);

	/**
	 * For list of richGroups filter all their group attributes and remove all which principal has no access to.
	 * Context usage is safe even for groups from different VOs.
	 *
	 * Context means same "combination of authz role for a group"+"attribute_urn (name)". Since privileges are resolved by roles on group and attribute type.
	 *
	 * if useContext is true: every attribute is unique in a context of authz roles combination and its URN. So for each combination of
	 * users authz roles granted for the group, attributes with same URN has same privilege.
	 *
	 * if useContext is false: every attribute is unique in context of group, which means every attribute for more groups need to be check separately,
	 * because for example groups can be from different vos where user has different authz (better authorization check, worse performance)
	 *
	 * @param sess
	 * @param richGroups
	 * @param resource optional resource param used for context
	 * @return list of RichGroups with only allowed attributes
	 * @throws InternalErrorException
	 */
	List<RichGroup> filterOnlyAllowedAttributes(PerunSession sess, List<RichGroup> richGroups, Resource resource, boolean useContext);

	/**
	 * For list of richGroups filter all their group attributes and remove all which principal has no access to.
	 * Context usage is safe even for groups from different VOs.
	 *
	 * Context means same "combination of authz role for a group"+"attribute_urn (name)". Since privileges are resolved by roles on group and attribute type.
	 *
	 * if useContext is true: every attribute is unique in a context of authz roles combination and its URN. So for each combination of
	 * users authz roles granted for the group, attributes with same URN has same privilege.
	 *
	 * if useContext is false: every attribute is unique in context of group, which means every attribute for more groups need to be check separately,
	 * because for example groups can be from different vos where user has different authz (better authorization check, worse performance)
	 *
	 * @param sess
	 * @param richGroups
	 * @param member optional member param used for context
	 * @param resource optional resource param used for context
	 * @return list of RichGroups with only allowed attributes
	 * @throws InternalErrorException
	 */
	List<RichGroup> filterOnlyAllowedAttributes(PerunSession sess, List<RichGroup> richGroups, Member member, Resource resource, boolean useContext);

	/**
	 * This method takes group and creates RichGroup containing all attributes
	 *
	 * @param sess
	 * @param group
	 * @return RichGroup
	 * @throws InternalErrorException
	 */
	RichGroup convertGroupToRichGroupWithAttributes(PerunSession sess, Group group);

	/**
	 * This method takes group and creates RichGroup containing selected attributes
	 *
	 * @param sess
	 * @param group
	 * @param attrNames list of selected attributes
	 * @return RichGroup
	 * @throws InternalErrorException
	 */
	RichGroup convertGroupToRichGroupWithAttributesByName(PerunSession sess, Group group, List<String> attrNames);

	/**
	 * This method takes list of groups and creates list of RichGroups containing all attributes
	 *
	 * @param sess
	 * @param groups list of groups
	 * @return RichGroup
	 * @throws InternalErrorException
	 */
	List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, List<Group> groups);

	/**
	 * This method takes list of groups and resource and then creates list of RichGroups containing all group and group-resource attributes
	 *
	 * @param sess
	 * @param resource specified resource to which are groups assigned
	 * @param groups list of groups
	 * @return list of RichGroups with attributes
	 * @throws InternalErrorException
	 * @throws GroupResourceMismatchException
	 */
	List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, Resource resource, List<Group> groups) throws GroupResourceMismatchException;

	/**
	 * This method takes list of groups and creates list of RichGroups containing selected attributes
	 *
	 * @param sess
	 * @param groups list of groups
	 * @param attrNames list of selected attributes
	 * @return RichGroup
	 * @throws InternalErrorException
	 */
	List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, List<Group> groups, List<String> attrNames);

	/**
	 * This method takes list of groups, resource and list of attrNames and then creates list of RichGroups containing
	 * all selected group and group-resource attributes by list (attributes from other namespaces are skipped).
	 * If attribute is in the list, it can be return with empty value if it is not set.
	 *
	 * @param sess
	 * @param resource
	 * @param groups
	 * @param attrNames list of selected attribute names,
	 *                  if it is null, return all possible non-empty attributes,
	 *                  empty list in attrNames means - no attributes needed
	 * @return list of RichGroups with selected attributes
	 * @throws InternalErrorException
	 * @throws GroupResourceMismatchException
	 */
	List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, Resource resource, List<Group> groups, List<String> attrNames) throws GroupResourceMismatchException;

	/**
	 * This method takes list of groups, resource, member and list of attrNames and then creates list of RichGroups containing
	 * all selected group, group-resource and member-group attributes filtered by list (attributes from other
	 * namespaces are skipped without any warning).
	 * If attribute with correct namespace is in the list, it will be return even with empty value (if no value exists).
	 *
	 * @param sess
	 * @param member member to get member-resource and member-group attributes for
	 * @param resource resource to get group-resource and member-resource attributes for
	 * @param groups to convert to richGroups and get group-resource and member-group attributes for
	 * @param attrNames list of selected attribute names,
	 *                  if it is null, return all possible non-empty attributes,
	 *                  empty list in attrNames means - no attributes needed
	 * @return list of RichGroups with selected attributes
	 * @throws InternalErrorException
	 * @throws GroupResourceMismatchException if group is not assigned to resource
	 * @throws MemberResourceMismatchException if member is not assigned to group
	 * @throws MemberGroupMismatchException if member is not in the same vo as group
	 */
	List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, Member member, Resource resource, List<Group> groups, List<String> attrNames) throws GroupResourceMismatchException, MemberResourceMismatchException, MemberGroupMismatchException;

	/**
	 * Get all RichGroups with selected attributes assigned to the resource.
	 *
	 * @param sess
	 * @param resource the resource to get assigned groups from it
	 * @param attrNames list of selected attribute names,
	 *                  if it is null, return all possible non-empty attributes,
	 *                  empty list in attrNames means - no attributes needed
	 * @return list of RichGroups with selected attributes assigned to the resource
	 * @throws InternalErrorException
	 */
	List<RichGroup> getRichGroupsWithAttributesAssignedToResource(PerunSession sess, Resource resource, List<String> attrNames);

	/**
	 * Get list of all richGroups with selected attributes assigned to the resource filtered by specific member.
	 * Allowed namespaces of attributes are group, group-resource, member-group and member-resource.
	 *
	 * Last step is filtration of attributes:
	 * Attributes are filtered by rights of user in session. User get only those selected attributes he has rights to read.
	 *
	 * @param sess
	 * @param member member used for filtering returned groups (groups have to contain this member to be returned)
	 * @param resource resource to get assigned groups for
	 * @param attrNames list of selected attribute names,
	 *                  if it is null, return all possible non-empty attributes,
	 *                  empty list in attrNames means - no attributes needed
	 * @return list of RichGroup objects with specific attributes specified by object Resource and object Member
	 * @throws InternalErrorException
	 */
	List<RichGroup> getRichGroupsWithAttributesAssignedToResource(PerunSession sess, Member member, Resource resource, List<String> attrNames);

	/**
	 * Return all RichGroups for specified member, containing selected attributes.
	 * "members" group is not included.
	 *
	 * Supported are attributes from these namespaces:
	 *  - group
	 *  - member-group
	 *
	 * @param sess internal session
	 * @param member the member to get the rich groups for
	 * @param attrNames list of selected attributes
	 * @return list of rich groups with selected attributes
	 * @throws InternalErrorException
	 */
	List<RichGroup> getMemberRichGroupsWithAttributesByNames(PerunSession sess, Member member, List<String> attrNames);

	/**
	 * Returns all RichGroups containing selected attributes
	 *
	 * @param sess
	 * @param vo
	 * @param attrNames if attrNames is null method will return RichGroups containing all attributes
	 * @return List of RichGroups
	 * @throws InternalErrorException
	 */
	List<RichGroup> getAllRichGroupsWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrNames);

	/**
	 * Returns RichSubGroups from parentGroup containing selected attributes (only 1 level subgroups)
	 *
	 * @param sess
	 * @param parentGroup
	 * @param attrNames if attrNames is null method will return RichGroups containing all attributes
	 * @return List of RichGroups
	 * @throws InternalErrorException
	 */
	List<RichGroup> getRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames);

	/**
	 * Returns all RichSubGroups from parentGroup containing selected attributes (all levels subgroups)
	 *
	 * @param sess
	 * @param parentGroup
	 * @param attrNames if attrNames is null method will return RichGroups containing all attributes
	 * @return List of RichGroups
	 * @throws InternalErrorException
	 */
	List<RichGroup> getAllRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames);

	/**
	 * Returns RichGroup selected by id containing selected attributes
	 *
	 * @param sess
	 * @param groupId
	 * @param attrNames if attrNames is null method will return RichGroup containing all attributes
	 * @return RichGroup
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 */
	RichGroup getRichGroupByIdWithAttributesByNames(PerunSession sess, int groupId, List<String> attrNames) throws GroupNotExistsException;

	/**
	 * This method will set timestamp, synchronization start time and exceptionMessage to group attributes for the group.
	 * Also log information about failed synchronization to auditer_log.
	 *
	 * IMPORTANT: This method runs in new transaction (because of using in synchronization of groups)
	 * This method is run in a new transaction to ensure successful saving of given information, in case of a rollback in previous transaction.
	 * However, this method cannot be used in method running in the nested transaction, where the group was changed in the database.
	 *
	 * Set timestamp to attribute "group_def_lastSynchronizationTimestamp"
	 * Set exception message to attribute "group_def_lastSynchronizationState"
	 * Set start time to attribute "group_def_startOfLastSuccessSynchronizationTimestamp"
	 *
	 * FailedDueToException is true means group synchronization failed completely.
	 * FailedDueToException is false means group synchronization is ok or finished with some errors (some members were not synchronized)
	 *
	 * @param sess perun session
	 * @param group the group for synchronization
	 * @param startTime of the synchronization
	 * @param failedDueToException if exception means fail of whole synchronization of this group or only problem with some data
	 * @param exceptionMessage message of an exception, ok if everything is ok
	 * @throws AttributeNotExistsException
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 */
	void saveInformationAboutGroupSynchronizationInNewTransaction(PerunSession sess, Group group, long startTime, boolean failedDueToException, String exceptionMessage) throws AttributeNotExistsException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, WrongAttributeValueException;

	/**
	 * This method will set timestamp, synchronization start time and exceptionMessage to group attributes for the group.
	 * Also log information about failed synchronization to auditer_log.
	 *
	 * IMPORTANT: This method runs in nested transaction so it can be used in another transaction
	 * With a nested transaction, this method can be used in method running in the nested transaction, where the group was changed in the database.
	 * However, rollback on the outer transaction, where this method is used, will revert saving of given information.
	 *
	 * Set timestamp to attribute "group_def_lastSynchronizationTimestamp"
	 * Set exception message to attribute "group_def_lastSynchronizationState"
	 * Set start time to attribute "group_def_startOfLastSuccessSynchronizationTimestamp"
	 *
	 * FailedDueToException is true means group synchronization failed completely.
	 * FailedDueToException is false means group synchronization is ok or finished with some errors (some members were not synchronized)
	 *
	 * @param sess perun session
	 * @param group the group for synchronization
	 * @param startTime of the synchronization
	 * @param failedDueToException if exception means fail of whole synchronization of this group or only problem with some data
	 * @param exceptionMessage message of an exception, ok if everything is ok
	 * @throws AttributeNotExistsException
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 */
	void saveInformationAboutGroupSynchronizationInNestedTransaction(PerunSession sess, Group group, long startTime, boolean failedDueToException, String exceptionMessage) throws AttributeNotExistsException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, WrongAttributeValueException;


	/**
	 * This method will set timestamp, state and exceptionMessage to group attributes for the group structure.
	 * Also log information about failed group structure synchronization to auditer_log.
	 *
	 * IMPORTANT: This method runs in new transaction (because of it being used in synchronization of groups structures)
	 * This method is run in a new transaction to ensure successful saving of given information, in case of a rollback in previous transaction.
	 * However, this method cannot be used in method running in the nested transaction, where the group was changed in the database.
	 *
	 * Set timestamp to attribute "group_def_lastGroupStructureSynchronizationTimestamp"
	 * Set exception message to attribute "group_def_lastGroupStructureSynchronizationState"
	 *
	 * FailedDueToException is true means group structure synchronization failed completely.
	 * FailedDueToException is false means group structure synchronization is ok or finished with some errors (some groups were not synchronized)
	 *
	 * @param sess perun session
	 * @param group the group structure for synchronization
	 * @param failedDueToException if exception means fail of whole synchronization of this group structure or only problem with some data
	 * @param exceptionMessage message of an exception, ok if everything is ok
	 * @throws AttributeNotExistsException
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 */
	void saveInformationAboutGroupStructureSynchronizationInNewTransaction(PerunSession sess, Group group, boolean failedDueToException, String exceptionMessage) throws AttributeNotExistsException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, WrongAttributeValueException;

	/**
	 * This method will set timestamp, state and exceptionMessage to group attributes for the group structure.
	 * Also log information about failed group structure synchronization to auditer_log.
	 *
	 * IMPORTANT: This method runs in nested transaction.
	 * With a nested transaction, this method can be used in method running in the nested transaction, where the group was changed in the database.
	 * However, rollback on the outer transaction, where this method is used, will revert saving of given information.
	 *
	 * Set timestamp to attribute "group_def_lastGroupStructureSynchronizationTimestamp"
	 * Set exception message to attribute "group_def_lastGroupStructureSynchronizationState"
	 *
	 * FailedDueToException is true means group structure synchronization failed completely.
	 * FailedDueToException is false means group structure synchronization is ok or finished with some errors (some groups were not synchronized)
	 *
	 * @param sess perun session
	 * @param group the group structure for synchronization
	 * @param failedDueToException if exception means fail of whole synchronization of this group structure or only problem with some data
	 * @param exceptionMessage message of an exception, ok if everything is ok
	 * @throws AttributeNotExistsException
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 */
	void saveInformationAboutGroupStructureSynchronizationInNestedTransaction(PerunSession sess, Group group, boolean failedDueToException, String exceptionMessage) throws AttributeNotExistsException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, WrongAttributeValueException;

	/**
	 * Get all groups in specific vo with assigned extSource
	 *
	 * @param sess
	 * @param source
	 * @param vo
	 * @return l
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsWithAssignedExtSourceInVo(PerunSession sess, ExtSource source, Vo vo);

	/**
	 * Method recalculates all relations between groups. Method recursively adds members from
	 * groups and all their relations. The method is called in case of:
	 * 1) added relation
	 * 2) added member
	 * 3) group creation
	 * 4) group movement
	 *
	 * @param sess perun session
	 * @param resultGroup group to which members are added or removed from
	 * @param changedMembers list of changed members which is passed as argument to add indirect members method.
	 *                       List contains records of added indirect members from operand group.
	 * @param sourceGroupId id of a group from which members originate
	 * @throws InternalErrorException
	 * @throws AlreadyMemberException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws GroupNotExistsException
	 */
	void addRelationMembers(PerunSession sess, Group resultGroup, List<Member> changedMembers, int sourceGroupId) throws AlreadyMemberException, WrongReferenceAttributeValueException, WrongAttributeValueException, GroupNotExistsException;

	/**
	 * Method recalculates all relations between groups. Method recursively removes members from
	 * groups and all their relations. The method is called in case of:
	 * 1) removed relation
	 * 2) removed member
	 * 3) group removal
	 * 4) group movement
	 *
	 * @param sess perun session
	 * @param resultGroup group to which members are added or removed from
	 * @param changedMembers list of changed members which is passed as argument to add indirect members method.
	 *                       List contains records of removed indirect members from operand group.
	 * @param sourceGroupId id of a group from which members originate
	 * @throws WrongReferenceAttributeValueException
	 * @throws NotGroupMemberException
	 * @throws WrongAttributeValueException
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 */
	void removeRelationMembers(PerunSession sess, Group resultGroup, List<Member> changedMembers, int sourceGroupId) throws WrongReferenceAttributeValueException, NotGroupMemberException, WrongAttributeValueException, GroupNotExistsException;

	/**
	 * Performs union operation on two groups. Members from operand group are added to result group as indirect.
	 *
	 * @param sess perun session
	 * @param resultGroup group to which members are added
	 * @param operandGroup group from which members are taken
	 * @param parentFlag if true union cannot be deleted; false otherwise (it flags relations created by hierarchical structure)
	 * @return result group
	 *
	 * @throws GroupRelationAlreadyExists
	 * @throws GroupRelationNotAllowed
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws GroupNotExistsException
	 */
	Group createGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup, boolean parentFlag) throws GroupRelationAlreadyExists, GroupRelationNotAllowed, WrongReferenceAttributeValueException, WrongAttributeValueException, GroupNotExistsException;

	/**
	 * Removes a union relation between two groups. All indirect members that originate from operand group are removed from result group.
	 *
	 * @param sess perun session
	 * @param resultGroup group from which members are removed
	 * @param operandGroup group which members are removed from result group
	 * @param parentFlag if true union cannot be deleted; false otherwise (it flags relations created by hierarchical structure)
	 *
	 * @throws GroupRelationDoesNotExist
	 * @throws GroupRelationCannotBeRemoved
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 */
	void removeGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup, boolean parentFlag) throws GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, GroupNotExistsException;

	/**
	 * Get list of group unions for specified group.
	 * @param sess perun session
	 * @param group group
	 * @param reverseDirection if false get all operand groups of requested result group
	 *                         if true get all result groups of requested operand group
	 * @return list of groups.
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getGroupUnions(PerunSession sess, Group group, boolean reverseDirection);

	/**
	 * Move one group structure under another group in same vo or as top level group
	 *
	 * @param sess             perun session
	 * @param destinationGroup group to which is moving group moved, if it's null group will be moved as top level group
	 * @param movingGroup      group which is moved to destination group
	 * @throws InternalErrorException
	 * @throws GroupMoveNotAllowedException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	void moveGroup(PerunSession sess, Group destinationGroup, Group movingGroup) throws GroupMoveNotAllowedException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Set member's status in given group to EXPIRED
	 *
	 * @param sess perun session
	 * @param member member whose status will be changed
	 * @param group group in which given member will be expired
	 * @throws InternalErrorException internal error
	 */
	void expireMemberInGroup(PerunSession sess, Member member, Group group);

	/**
	 * Set member's status in given group to VALID
	 *
	 * @param sess perun session
	 * @param member member whose status will be changed
	 * @param group group in which given member will be validated
	 * @throws InternalErrorException internal error
	 */
	void validateMemberInGroup(PerunSession sess, Member member, Group group);

	/**
	 * Returns members direct status in given group. This method doesn't
	 * calculate status from subgroups!
	 * If there is no relation, null is returned.
	 *
	 * @param session session
	 * @param member member
	 * @param group group
	 * @return status of member in given group
	 * @throws InternalErrorException internal error
	 */
	MemberGroupStatus getDirectMemberGroupStatus(PerunSession session, Member member, Group group);

	/**
	 * Returns total member's status in given group.
	 * If there is no relation, null is returned.
	 *
	 * @param session session
	 * @param member member
	 * @param group group
	 * @return total status of member in given group
	 * @throws InternalErrorException internal error
	 */
	MemberGroupStatus getTotalMemberGroupStatus(PerunSession session, Member member, Group group);

	/**
	 * Calculates the state of given member in given group and calls
	 * this method recursively for all parent groups.
	 *
	 * @param member member
	 * @param group group
	 * @throws InternalErrorException internal error
	 */
	void recalculateMemberGroupStatusRecursively(PerunSession sess, Member member, Group group);

	/**
	 * Extend member membership in given group using membershipExpirationRules attribute defined in Group.
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @throws InternalErrorException internal error
	 * @throws ExtendMembershipException extend membership exception
	 */
	void extendMembershipInGroup(PerunSession sess, Member member, Group group) throws ExtendMembershipException;

	/**
	 * Returns true if member in given group can extend membership or if no rules were set for the membershipExpiration
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @return true if given member can extend membership in given group  or if no rules were set for the
	 * membership expiration, false otherwise
	 */
	boolean canExtendMembershipInGroup(PerunSession sess, Member member, Group group);

	/**
	 * Returns true if member in given group can extend membership or throws exception with reason why use can't extends membership
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @throws ExtendMembershipException reason why user can't extend membership
	 * @return true if given member can extend membership in given group or throws exception with reason why not
	 */
	boolean canExtendMembershipInGroupWithReason(PerunSession sess, Member member, Group group) throws ExtendMembershipException;

	/**
	 * Synchronize a group structure with an external source group structure under the group.
	 *
	 * @param sess
	 * @param group base group under which will be synchronized structure of groups
	 * @return List of strings with skipped groups with reasons why were skipped
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws ExtSourceNotExistsException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	List<String> synchronizeGroupStructure(PerunSession sess, Group group) throws AttributeNotExistsException, WrongAttributeAssignmentException, ExtSourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Check if the group or its subgroups are defined as synchronized from an external source at this moment.
	 *
	 * @param session
	 * @param group
	 * @return
	 * @throws InternalErrorException
	 */
	boolean isGroupInStructureSynchronizationTree(PerunSession session, Group group);

	/**
	 * Check if the group is defined as synchronized from an external source at this moment.
	 *
	 * @param session
	 * @param group
	 * @return
	 * @throws InternalErrorException
	 */
	boolean isGroupSynchronizedFromExternallSource(PerunSession session, Group group);

	/**
	 * Check if there is a subgroup of the group, which is defined as synchronized from an external source at this moment.
	 *
	 * @param session
	 * @param group
	 * @return
	 * @throws InternalErrorException
	 */
	boolean hasGroupSynchronizedChild(PerunSession session, Group group);

	/**
	 *  Get list of groups where the given group is given the admin role.
	 *
	 * @param perunSession
	 * @param Group with the admin role.
	 * @return List of administered groups.
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsWhereGroupIsAdmin(PerunSession perunSession, Group group);

	/**
	 *  Get list of VOs where the given group is given the admin role.
	 *
	 * @param perunSession
	 * @param Group with the admin role.
	 * @return List of administered VOs.
	 * @throws InternalErrorException
	 */
	List<Vo> getVosWhereGroupIsAdmin(PerunSession perunSession, Group group);

	/**
	 *  Get list of facilities where the given group is given the admin role.
	 *
	 * @param perunSession
	 * @param Group with the admin role.
	 * @return List of administered facilities.
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesWhereGroupIsAdmin(PerunSession perunSession, Group group);

	/**
	 * Reactivates member in group and sets its status to VALID.
	 *
	 * @param sess perun session
	 * @param member member
	 * @param group group
	 * @throws InternalErrorException internal error
	 * @throws MemberNotExistsException if given member is not member of given group
	 */
	void reactivateMember(PerunSession sess, Member member, Group group) throws MemberNotExistsException;

	/**
	 * Returns all groups which can be registered into during vo registration.
	 *
	 * @param sess session
	 * @param vo vo
	 * @return list of groups
	 */
	List<Group> getGroupsForAutoRegistration(PerunSession sess, Vo vo);

	/**
	 * Deletes groups from a list of groups which can be registered into during vo registration.
	 *
	 * @param sess session
	 * @param groups list of groups
	 */
	void deleteGroupsFromAutoRegistration(PerunSession sess, List<Group> groups);

	/**
	 * Adds groups to a list of groups which can be registered into during vo registration.
	 *
	 * @param sess session
	 * @param groups list of groups
	 */
	void addGroupsToAutoRegistration(PerunSession sess, List<Group> groups);
}
