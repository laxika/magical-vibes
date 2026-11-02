package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTransformedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.rules.PermanentChange;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** One factory per trigger event, for {@code Card.addTrigger}. */
public final class Triggers {

    /** A creature. */
    public static final PermanentPredicate CREATURE = new PermanentIsCreaturePredicate();
    /** A nontoken creature. */
    public static final PermanentPredicate NONTOKEN_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(), new PermanentNotPredicate(new PermanentIsTokenPredicate())));
    /** An artifact or a creature. */
    public static final PermanentPredicate ARTIFACT_OR_CREATURE = new PermanentAnyOfPredicate(List.of(
            new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate()));
    /** A creature or a planeswalker. */
    public static final PermanentPredicate CREATURE_OR_PLANESWALKER = new PermanentAnyOfPredicate(List.of(
            new PermanentIsCreaturePredicate(), new PermanentIsPlaneswalkerPredicate()));

    /** An artifact. */
    public static final PermanentPredicate ARTIFACT = new PermanentIsArtifactPredicate();
    /** An enchantment. */
    public static final PermanentPredicate ENCHANTMENT = new PermanentIsEnchantmentPredicate();
    /** A land. */
    public static final PermanentPredicate LAND = new PermanentIsLandPredicate();
    /** A noncreature permanent. */
    public static final PermanentPredicate NONCREATURE = new PermanentNotPredicate(new PermanentIsCreaturePredicate());
    /** An Aura or an Equipment. */
    public static final PermanentPredicate AURA_OR_EQUIPMENT = new PermanentAnyOfPredicate(List.of(
            new PermanentHasSubtypePredicate(CardSubtype.AURA), new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)));
    /** A noncreature artifact. */
    public static final PermanentPredicate NONCREATURE_ARTIFACT = new PermanentAllOfPredicate(List.of(
            new PermanentIsArtifactPredicate(), new PermanentNotPredicate(new PermanentIsCreaturePredicate())));
    /** A nontoken artifact. */
    public static final PermanentPredicate NONTOKEN_ARTIFACT = new PermanentAllOfPredicate(List.of(
            new PermanentIsArtifactPredicate(), new PermanentNotPredicate(new PermanentIsTokenPredicate())));
    /** A nonland permanent. */
    public static final PermanentPredicate NONLAND = new PermanentNotPredicate(new PermanentIsLandPredicate());
    /** A land card: a land that isn't a token. */
    public static final PermanentPredicate LAND_CARD = new PermanentAllOfPredicate(List.of(
            new PermanentIsLandPredicate(), new PermanentNotPredicate(new PermanentIsTokenPredicate())));
    /** A permanent card: an artifact, battle, creature, enchantment, land or planeswalker card (CR 110.4a). */
    public static final PermanentPredicate PERMANENT_CARD = new PermanentAllOfPredicate(List.of(
            new PermanentNotPredicate(new PermanentIsTokenPredicate()),
            new PermanentAnyOfPredicate(List.of(new PermanentIsArtifactPredicate(), new PermanentIsBattlePredicate(),
                    new PermanentIsCreaturePredicate(), new PermanentIsEnchantmentPredicate(),
                    new PermanentIsLandPredicate(), new PermanentIsPlaneswalkerPredicate()))));
    /** A black card. */
    public static final PermanentPredicate BLACK_CARD = new PermanentAllOfPredicate(List.of(
            new PermanentNotPredicate(new PermanentIsTokenPredicate()),
            new PermanentColorInPredicate(Set.of(CardColor.BLACK))));
    /** A nonblack card. */
    public static final PermanentPredicate NONBLACK_CARD = new PermanentAllOfPredicate(List.of(
            new PermanentNotPredicate(new PermanentIsTokenPredicate()),
            new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))));
    /** A token. */
    public static final PermanentPredicate TOKEN = new PermanentIsTokenPredicate();
    /** A nontoken permanent. */
    public static final PermanentPredicate NONTOKEN = new PermanentNotPredicate(new PermanentIsTokenPredicate());
    /** An Equipment. */
    public static final PermanentPredicate EQUIPMENT = new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT);
    /** A creature or an enchantment. */
    public static final PermanentPredicate CREATURE_OR_ENCHANTMENT = new PermanentAnyOfPredicate(List.of(
            new PermanentIsCreaturePredicate(), new PermanentIsEnchantmentPredicate()));
    /** A permanent showing its transformed face. */
    public static final PermanentPredicate TRANSFORMED = new PermanentIsTransformedPredicate();

    /** Every zone but the battlefield and the graveyard: "from anywhere other than the battlefield". */
    private static final Set<Zone> NOT_BATTLEFIELD = EnumSet.complementOf(EnumSet.of(Zone.BATTLEFIELD, Zone.GRAVEYARD));

    private Triggers() {
    }

    /** "When [object] dies": put into a graveyard from the battlefield (CR 700.4). */
    public static ZoneChangeTrigger dies(ObjectRelation relation) {
        return dies(null, relation);
    }

    /**
     * "Whenever [a filter object] dies": {@code dies(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)} for
     * "whenever another creature you control dies". The object is judged as it last existed on the battlefield
     * (CR 603.10a).
     */
    public static ZoneChangeTrigger dies(PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(Zone.BATTLEFIELD), Zone.GRAVEYARD, Set.of());
    }

    /** "Whenever [a creature] dies", for {@code relation}: {@code dies(CREATURE, relation)}. */
    public static ZoneChangeTrigger creatureDies(ObjectRelation relation) {
        return dies(CREATURE, relation);
    }

    /** "Whenever [a creature that also matches {@code filter}] dies": "whenever this or another Zombie dies". */
    public static ZoneChangeTrigger creatureDies(PermanentPredicate filter, ObjectRelation relation) {
        return dies(new PermanentAllOfPredicate(List.of(CREATURE, filter)), relation);
    }

    /** "Whenever [a nontoken creature] dies", for {@code relation}. */
    public static ZoneChangeTrigger nontokenCreatureDies(ObjectRelation relation) {
        return dies(NONTOKEN_CREATURE, relation);
    }

    /** "Whenever [a nontoken creature that also matches {@code filter}] dies". */
    public static ZoneChangeTrigger nontokenCreatureDies(PermanentPredicate filter, ObjectRelation relation) {
        return dies(new PermanentAllOfPredicate(List.of(NONTOKEN_CREATURE, filter)), relation);
    }

    /**
     * "When you sacrifice [object]": its sacrifice moves it from the battlefield (CR 701.21a), wherever a
     * replacement effect sends it.
     */
    public static ZoneChangeTrigger sacrificed(ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, Set.of(Zone.BATTLEFIELD), null, Set.of(ZoneChangeCause.SACRIFICE));
    }

    /** "When [object] leaves the battlefield" (CR 603.6c). */
    public static ZoneChangeTrigger leavesBattlefield(ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, Set.of(Zone.BATTLEFIELD), null, Set.of());
    }

    /** "When [object] is put into a graveyard from [zone]". */
    public static ZoneChangeTrigger putIntoGraveyardFrom(Zone from, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, Set.of(from), Zone.GRAVEYARD, Set.of());
    }

    /**
     * "When [object] is put into a graveyard from anywhere". It is never a leaves-the-battlefield ability, even
     * for a move from the battlefield (CR 603.6c), so the source's own move triggers it from the graveyard.
     */
    public static ZoneChangeTrigger putIntoGraveyardFromAnywhere(ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, Set.of(), Zone.GRAVEYARD, Set.of());
    }

    /**
     * "When this spell is put into a graveyard during its resolution", as haunt reads on an instant or sorcery
     * (CR 702.55a). A spell countered, or one that doesn't resolve because its targets became illegal, isn't.
     */
    public static ZoneChangeTrigger putIntoGraveyardWhileResolving(ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, Set.of(Zone.STACK), Zone.GRAVEYARD, Set.of(ZoneChangeCause.RESOLUTION));
    }

    /** "When [object] is milled": put into a graveyard from a library by a mill. */
    public static ZoneChangeTrigger milled(ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, Set.of(Zone.LIBRARY), Zone.GRAVEYARD, Set.of(ZoneChangeCause.MILL));
    }

    /**
     * "Whenever you sacrifice [a filter object]", "whenever a player sacrifices …": its sacrifice moves it from the
     * battlefield (CR 701.21a), wherever a replacement effect sends it. A player sacrifices only a permanent they
     * control, so the sacrificing player is its controller. It looks back in time (CR 603.10a).
     */
    public static ZoneChangeTrigger sacrificed(PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(Zone.BATTLEFIELD), null,
                Set.of(ZoneChangeCause.SACRIFICE));
    }

    /** "Whenever [a filter permanent] is sacrificed or destroyed": either way it leaves the battlefield. */
    public static ZoneChangeTrigger sacrificedOrDestroyed(PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(Zone.BATTLEFIELD), null,
                Set.of(ZoneChangeCause.SACRIFICE, ZoneChangeCause.DESTROY));
    }

    /** "Whenever [a filter permanent] is destroyed" (CR 701.8a), wherever a replacement effect sends it. */
    public static ZoneChangeTrigger destroyed(PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(Zone.BATTLEFIELD), null,
                Set.of(ZoneChangeCause.DESTROY));
    }

    /** "Whenever [a filter object] leaves the battlefield" (CR 603.6c). */
    public static ZoneChangeTrigger leavesBattlefield(PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(Zone.BATTLEFIELD), null, Set.of());
    }

    /** "Whenever [a filter object] leaves the battlefield without dying": for any zone but a graveyard. */
    public static ZoneChangeTrigger leavesBattlefieldWithoutDying(PermanentPredicate filter,
                                                                  ObjectRelation relation) {
        return leavesBattlefield(filter, relation).notTo(Set.of(Zone.GRAVEYARD));
    }

    /** "Whenever [a filter object] is put into a graveyard or exiled from the battlefield". */
    public static ZoneChangeTrigger putIntoGraveyardOrExileFromBattlefield(PermanentPredicate filter,
                                                                           ObjectRelation relation) {
        return leavesBattlefield(filter, relation)
                .notTo(EnumSet.complementOf(EnumSet.of(Zone.GRAVEYARD, Zone.EXILE)));
    }

    /** "Whenever [a filter object] is put into a graveyard from [zone]". */
    public static ZoneChangeTrigger putIntoGraveyardFrom(Zone from, PermanentPredicate filter,
                                                         ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(from), Zone.GRAVEYARD, Set.of());
    }

    /**
     * "Whenever [a filter card] is put into a graveyard from anywhere". It is never a leaves-the-battlefield
     * ability, even for a move from the battlefield (CR 603.6c).
     */
    public static ZoneChangeTrigger putIntoGraveyardFromAnywhere(PermanentPredicate filter,
                                                                 ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(), Zone.GRAVEYARD, Set.of());
    }

    /** "Whenever [a filter card] is put into a graveyard from anywhere other than the battlefield". */
    public static ZoneChangeTrigger putIntoGraveyardFromAnywhereButBattlefield(PermanentPredicate filter,
                                                                               ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, NOT_BATTLEFIELD, Zone.GRAVEYARD, Set.of());
    }

    /** "Whenever [a filter card] is put into a graveyard from anywhere but [zone]". */
    public static ZoneChangeTrigger putIntoGraveyardFromAnywhereBut(Zone except, PermanentPredicate filter,
                                                                    ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, EnumSet.complementOf(EnumSet.of(except, Zone.GRAVEYARD)),
                Zone.GRAVEYARD, Set.of());
    }

    /** "Whenever [a filter card] is milled": put into a graveyard from a library by a mill. */
    public static ZoneChangeTrigger milled(PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(Zone.LIBRARY), Zone.GRAVEYARD,
                Set.of(ZoneChangeCause.MILL));
    }

    /**
     * "Whenever [a player] mills [a filter card]": put from a library into a graveyard by a mill, or wherever a
     * replacement effect sends it (CR 701.17a).
     */
    public static ZoneChangeTrigger mills(PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(Zone.LIBRARY), null, Set.of(ZoneChangeCause.MILL));
    }

    /** "Whenever [a filter object] is put into exile from [zones]"; no zones for anywhere. */
    public static ZoneChangeTrigger exiledFrom(Set<Zone> from, PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, from, Zone.EXILE, Set.of());
    }

    /**
     * "Whenever [a filter card] leaves [a graveyard]", for any destination. It looks back in time
     * (CR 603.10a).
     */
    public static ZoneChangeTrigger leavesGraveyard(PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(Zone.GRAVEYARD), null, Set.of());
    }

    /** "Whenever [a filter object] is put into [a hand] from [zone]". */
    public static ZoneChangeTrigger putIntoHandFrom(Zone from, PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(from), Zone.HAND, Set.of());
    }

    /** "Whenever [a filter card] is put into [a library] from anywhere". */
    public static ZoneChangeTrigger putIntoLibrary(PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(), Zone.LIBRARY, Set.of());
    }

    /** "Whenever [a filter card] is put into the command zone from anywhere". */
    public static ZoneChangeTrigger putIntoCommandZone(PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(), Zone.COMMAND, Set.of());
    }

    /**
     * "When [object] enters" (CR 603.6a): {@code enters(ObjectRelation.THIS)} for "when this creature enters", from any
     * zone and in any way.
     */
    public static ZoneChangeTrigger enters(ObjectRelation relation) {
        return enters(null, relation);
    }

    /** "Whenever [a filter permanent] enters" (CR 603.6a). */
    public static ZoneChangeTrigger enters(PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(), Zone.BATTLEFIELD, Set.of());
    }

    /**
     * "Whenever [a player] creates one or more [filter] tokens": the tokens a creation puts onto the battlefield
     * (CR 701.7a). Unlike "whenever a token enters", it isn't caused by the tokens entering.
     */
    public static ZoneChangeTrigger tokensCreated(PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(), Zone.BATTLEFIELD, Set.of(ZoneChangeCause.CREATE));
    }

    /** "Whenever [a filter permanent] enters from [zone]". */
    public static ZoneChangeTrigger entersFrom(Zone from, PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(from), Zone.BATTLEFIELD, Set.of());
    }

    /** "Whenever [a filter permanent] phases out" (CR 702.26a). It looks back in time (CR 603.10b). */
    public static PermanentChangeTrigger phasesOut(PermanentPredicate filter, ObjectRelation relation) {
        return new PermanentChangeTrigger(relation, filter, Set.of(PermanentChange.PHASED_OUT));
    }

    /** "Whenever [a filter permanent] phases in" (CR 702.26a). */
    public static PermanentChangeTrigger phasesIn(PermanentPredicate filter, ObjectRelation relation) {
        return new PermanentChangeTrigger(relation, filter, Set.of(PermanentChange.PHASED_IN));
    }

    /** "Whenever [a filter permanent] transforms", in either direction (CR 701.27a). */
    public static PermanentChangeTrigger transforms(PermanentPredicate filter, ObjectRelation relation) {
        return new PermanentChangeTrigger(relation, filter,
                Set.of(PermanentChange.TRANSFORMED_TO_BACK, PermanentChange.TRANSFORMED_TO_FRONT));
    }

    /** "When [this] transforms into [its back face]". */
    public static PermanentChangeTrigger transformsToBackFace(ObjectRelation relation) {
        return new PermanentChangeTrigger(relation, null, Set.of(PermanentChange.TRANSFORMED_TO_BACK));
    }

    /** "When [this] transforms into [its front face]". */
    public static PermanentChangeTrigger transformsToFrontFace(ObjectRelation relation) {
        return new PermanentChangeTrigger(relation, null, Set.of(PermanentChange.TRANSFORMED_TO_FRONT));
    }

    /** "When [a filter permanent] is turned face up" (CR 708.8). */
    public static PermanentChangeTrigger turnedFaceUp(PermanentPredicate filter, ObjectRelation relation) {
        return new PermanentChangeTrigger(relation, filter, Set.of(PermanentChange.TURNED_FACE_UP));
    }
}
