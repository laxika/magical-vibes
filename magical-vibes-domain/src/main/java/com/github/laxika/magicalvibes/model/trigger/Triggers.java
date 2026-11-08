package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.StateTriggerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsGoadedPredicate;
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
import java.util.Objects;
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
    /** A planeswalker. */
    public static final PermanentPredicate PLANESWALKER = new PermanentIsPlaneswalkerPredicate();
    /** A Saga. */
    public static final PermanentPredicate SAGA = new PermanentHasSubtypePredicate(CardSubtype.SAGA);
    /** A Human creature. */
    public static final PermanentPredicate HUMAN_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(), new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
    /** A Hero creature. */
    public static final PermanentPredicate HERO_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(), new PermanentHasSubtypePredicate(CardSubtype.HERO)));
    /** A non-Hydra creature. */
    public static final PermanentPredicate NON_HYDRA_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.HYDRA))));

    /** An artifact token. */
    public static final PermanentPredicate ARTIFACT_TOKEN = new PermanentAllOfPredicate(List.of(
            new PermanentIsArtifactPredicate(), new PermanentIsTokenPredicate()));

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

    /**
     * "Whenever [a filter permanent] is put into a graveyard from the battlefield, if it wasn't sacrificed" (Urza's
     * Miter): a death caused by anything but a sacrifice (CR 701.21a). Whether it was sacrificed can't change once it
     * died, so the "if" is part of the trigger event.
     */
    public static ZoneChangeTrigger diesWithoutBeingSacrificed(PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(Zone.BATTLEFIELD), Zone.GRAVEYARD,
                EnumSet.complementOf(EnumSet.of(ZoneChangeCause.SACRIFICE)));
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

    /** "At the beginning of [player's] upkeep": {@code upkeep(ActivePlayer.YOU)} for "your upkeep". */
    public static StepTrigger upkeep(ActivePlayer player) {
        return new StepTrigger(StepTrigger.Moment.UPKEEP, player);
    }

    /** "At the beginning of [player's] draw step". */
    public static StepTrigger drawStep(ActivePlayer player) {
        return new StepTrigger(StepTrigger.Moment.DRAW_STEP, player);
    }

    /** "At the beginning of [player's] precombat main phase", the first main phase of the turn. */
    public static StepTrigger precombatMainPhase(ActivePlayer player) {
        return new StepTrigger(StepTrigger.Moment.PRECOMBAT_MAIN_PHASE, player);
    }

    /** "At the beginning of combat on [player's] turn". */
    public static StepTrigger beginningOfCombat(ActivePlayer player) {
        return new StepTrigger(StepTrigger.Moment.BEGINNING_OF_COMBAT, player);
    }

    /** "At end of combat", on any player's turn. */
    public static StepTrigger endOfCombat() {
        return new StepTrigger(StepTrigger.Moment.END_OF_COMBAT, ActivePlayer.ANY);
    }

    /** "At the beginning of [player's] postcombat main phase", every one of them. */
    public static StepTrigger postcombatMainPhase(ActivePlayer player) {
        return new StepTrigger(StepTrigger.Moment.POSTCOMBAT_MAIN_PHASE, player);
    }

    /** "At the beginning of [player's] second main phase", the turn's second main phase only. */
    public static StepTrigger secondMainPhase(ActivePlayer player) {
        return new StepTrigger(StepTrigger.Moment.SECOND_MAIN_PHASE, player);
    }

    /** "At the beginning of [player's] end step": {@code endStep(ActivePlayer.ANY)} for "each end step". */
    public static StepTrigger endStep(ActivePlayer player) {
        return new StepTrigger(StepTrigger.Moment.END_STEP, player);
    }

    /** "Whenever you cast a spell". */
    public static SpellTrigger youCastSpell() {
        return new SpellTrigger(SpellTrigger.SpellEvent.CAST, PlayerRelation.YOU);
    }

    /** "Whenever an opponent casts a spell". */
    public static SpellTrigger opponentCastsSpell() {
        return new SpellTrigger(SpellTrigger.SpellEvent.CAST, PlayerRelation.OPPONENT);
    }

    /** "Whenever a player casts a spell". */
    public static SpellTrigger playerCastsSpell() {
        return new SpellTrigger(SpellTrigger.SpellEvent.CAST, PlayerRelation.ANY);
    }

    /** "When you cast this spell", functioning from the stack. */
    public static SpellTrigger youCastThisSpell() {
        return new SpellTrigger(SpellTrigger.SpellEvent.CAST_THIS, PlayerRelation.YOU);
    }

    /** "Whenever you copy a spell". */
    public static SpellTrigger youCopySpell() {
        return new SpellTrigger(SpellTrigger.SpellEvent.COPIED, PlayerRelation.YOU);
    }

    /** "Whenever an opponent copies a spell". */
    public static SpellTrigger opponentCopiesSpell() {
        return new SpellTrigger(SpellTrigger.SpellEvent.COPIED, PlayerRelation.OPPONENT);
    }

    /** "Whenever you counter a spell". */
    public static SpellTrigger youCounterSpell() {
        return new SpellTrigger(SpellTrigger.SpellEvent.COUNTERS, PlayerRelation.YOU);
    }

    /** "Whenever a spell you control is countered". */
    public static SpellTrigger yourSpellCountered() {
        return new SpellTrigger(SpellTrigger.SpellEvent.COUNTERED, PlayerRelation.YOU);
    }

    /** "When this spell is countered or doesn't resolve because its targets are illegal", from the stack. */
    public static SpellTrigger thisSpellCounteredOrFizzles() {
        return new SpellTrigger(SpellTrigger.SpellEvent.THIS_COUNTERED_OR_FIZZLES, PlayerRelation.YOU);
    }

    /** "Whenever you play a land", this one included. */
    public static LandPlayTrigger youPlayLand() {
        return new LandPlayTrigger(ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever you play another land". */
    public static LandPlayTrigger youPlayAnotherLand() {
        return new LandPlayTrigger(ObjectRelation.ANOTHER_YOU_CONTROL);
    }

    /** "Whenever an opponent plays a land". */
    public static LandPlayTrigger opponentPlaysLand() {
        return new LandPlayTrigger(ObjectRelation.OPPONENT_CONTROLS);
    }

    /** "Whenever you activate an ability", mana abilities included. */
    public static AbilityTrigger youActivateAbility() {
        return new AbilityTrigger(AbilityTrigger.AbilityEvent.ACTIVATED, PlayerRelation.YOU);
    }

    /** "Whenever an opponent activates an ability", mana abilities included. */
    public static AbilityTrigger opponentActivatesAbility() {
        return new AbilityTrigger(AbilityTrigger.AbilityEvent.ACTIVATED, PlayerRelation.OPPONENT);
    }

    /** "Whenever you activate an ability that isn't a mana ability". */
    public static AbilityTrigger youActivateNonManaAbility() {
        return new AbilityTrigger(AbilityTrigger.AbilityEvent.NONMANA_ACTIVATED, PlayerRelation.YOU);
    }

    /**
     * "Whenever an ability of [a permanent] is activated, if it isn't a mana ability", whoever activates it: narrowed
     * by {@link AbilityTrigger#withSource} ("an ability of equipped creature", Illusionist's Bracers).
     */
    public static AbilityTrigger playerActivatesNonManaAbility() {
        return new AbilityTrigger(AbilityTrigger.AbilityEvent.NONMANA_ACTIVATED, PlayerRelation.ANY);
    }

    /** "Whenever an opponent activates an ability that isn't a mana ability". */
    public static AbilityTrigger opponentActivatesNonManaAbility() {
        return new AbilityTrigger(AbilityTrigger.AbilityEvent.NONMANA_ACTIVATED, PlayerRelation.OPPONENT);
    }

    /** "Whenever you activate an exhaust ability". */
    public static AbilityTrigger youActivateExhaustAbility() {
        return new AbilityTrigger(AbilityTrigger.AbilityEvent.EXHAUST_ACTIVATED, PlayerRelation.YOU);
    }

    /** "Whenever you activate an embalm or eternalize ability". */
    public static AbilityTrigger youActivateEternalizeOrEmbalm() {
        return new AbilityTrigger(AbilityTrigger.AbilityEvent.ETERNALIZE_OR_EMBALM_ACTIVATED, PlayerRelation.YOU);
    }

    /** "Whenever a mana ability of this permanent resolves". */
    public static AbilityTrigger thisManaAbilityResolves() {
        return new AbilityTrigger(AbilityTrigger.AbilityEvent.THIS_MANA_ABILITY_RESOLVES, PlayerRelation.YOU);
    }

    /**
     * "Whenever this creature attacks"; on an Aura or Equipment, "whenever enchanted (equipped) creature attacks" or
     * the ability it grants that creature.
     */
    public static CombatTrigger attacks() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ATTACKS, ObjectRelation.THIS);
    }

    /** "Whenever a creature you control attacks", this one included. */
    public static CombatTrigger creatureYouControlAttacks() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ATTACKS, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a creature attacks". */
    public static CombatTrigger creatureAttacks() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ATTACKS, ObjectRelation.ANY);
    }

    /** "Whenever a creature attacks you or a planeswalker you control". */
    public static CombatTrigger creatureAttacksYouOrYourPlaneswalker() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ATTACKS_YOU_OR_YOUR_PLANESWALKER, ObjectRelation.ANY);
    }

    /** "Whenever a creature attacks you". */
    public static CombatTrigger creatureAttacksYou() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ATTACKS_YOU, ObjectRelation.ANY);
    }

    /** "Whenever a creature an opponent controls attacks, if you're the defending player". */
    public static CombatTrigger creatureAttacksWhileYouDefend() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ATTACKS_WHILE_YOU_DEFEND, ObjectRelation.OPPONENT_CONTROLS);
    }

    /**
     * "Whenever you attack": one or more creatures you control attack. With {@link CombatTrigger#withFilter}, "whenever
     * you attack with one or more Wraiths": the ability triggers only if a declared attacker matches the filter, and
     * "they" are the matching attackers (the stack entry's attacking snapshots and triggering permanents).
     */
    public static CombatTrigger youAttack() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ONE_OR_MORE_ATTACK, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever you attack a player", once for each player attacked. */
    public static CombatTrigger youAttackPlayer() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ONE_OR_MORE_ATTACK_A_PLAYER, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever you attack a player or planeswalker", once for each one attacked. */
    public static CombatTrigger youAttackPlayerOrPlaneswalker() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ONE_OR_MORE_ATTACK_A_PLAYER_OR_PLANESWALKER,
                ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a player attacks": one or more creatures attack. */
    public static CombatTrigger playerAttacks() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ONE_OR_MORE_ATTACK, ObjectRelation.ANY);
    }

    /**
     * "Whenever a player attacks one of your opponents" / "whenever one or more creatures attack one of your opponents":
     * once for each opponent attacked. The stack entry's attacked target is that opponent ("that opponent", "defending
     * player"), its non-targeting target the attacking player ("that attacking player"); an intervening "if" about the
     * attacked opponent reads it as the attacked target (Breena, the Demagogue).
     */
    public static CombatTrigger playerAttacksOneOfYourOpponents() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ONE_OR_MORE_ATTACK_YOUR_OPPONENT, ObjectRelation.ANY);
    }

    /**
     * "Whenever an opponent attacks another one of your opponents": {@link #playerAttacksOneOfYourOpponents()} where the
     * attacking player is an opponent, once for each other opponent attacked.
     */
    public static CombatTrigger opponentAttacksAnotherOpponent() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ONE_OR_MORE_ATTACK_YOUR_OPPONENT,
                ObjectRelation.OPPONENT_CONTROLS);
    }

    /** "Whenever one or more creatures attack you". */
    public static CombatTrigger creaturesAttackYou() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ONE_OR_MORE_ATTACK_YOU, ObjectRelation.ANY);
    }

    /** "Whenever an opponent attacks a planeswalker you control with one or more creatures". */
    public static CombatTrigger opponentAttacksYourPlaneswalker() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ONE_OR_MORE_ATTACK_YOUR_PLANESWALKER,
                ObjectRelation.OPPONENT_CONTROLS);
    }

    /**
     * An attacking creature you control causes a triggered ability of its own to trigger, or this creature enlists a
     * creature.
     */
    public static CombatTrigger attackingCreatureYouControlAbilityTriggers() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ATTACKING_CREATURE_ABILITY_TRIGGERS,
                ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever this creature attacks and isn't blocked". */
    public static CombatTrigger attacksUnblocked() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ATTACKS_UNBLOCKED, ObjectRelation.THIS);
    }

    /** "Whenever enchanted creature attacks and isn't blocked". */
    public static CombatTrigger enchantedCreatureAttacksUnblocked() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ATTACKS_UNBLOCKED, ObjectRelation.ENCHANTED);
    }

    /** "Whenever a creature you control attacks and isn't blocked". */
    public static CombatTrigger creatureYouControlAttacksUnblocked() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ATTACKS_UNBLOCKED, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever one or more creatures an opponent controls attack you and aren't blocked". */
    public static CombatTrigger creaturesAttackYouUnblocked() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ONE_OR_MORE_ATTACK_YOU_UNBLOCKED,
                ObjectRelation.OPPONENT_CONTROLS);
    }

    /** "Whenever this creature blocks"; on an Aura or Equipment, the creature it is attached to. */
    public static CombatTrigger blocks() {
        return new CombatTrigger(CombatTrigger.CombatEvent.BLOCKS, ObjectRelation.THIS);
    }

    /** "Whenever this creature blocks two or more creatures". */
    public static CombatTrigger blocksTwoOrMore() {
        return new CombatTrigger(CombatTrigger.CombatEvent.BLOCKS_TWO_OR_MORE, ObjectRelation.THIS);
    }

    /** "Whenever a creature blocks". */
    public static CombatTrigger creatureBlocks() {
        return new CombatTrigger(CombatTrigger.CombatEvent.BLOCKS, ObjectRelation.ANY);
    }

    /** "Whenever one or more creatures block". */
    public static CombatTrigger creaturesBlock() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ONE_OR_MORE_BLOCK, ObjectRelation.ANY);
    }

    /** "Whenever this creature becomes blocked"; on an Aura or Equipment, the creature it is attached to. */
    public static CombatTrigger becomesBlocked() {
        return new CombatTrigger(CombatTrigger.CombatEvent.BECOMES_BLOCKED, ObjectRelation.THIS);
    }

    /** "Whenever a creature you control becomes blocked", this one included. */
    public static CombatTrigger creatureYouControlBecomesBlocked() {
        return new CombatTrigger(CombatTrigger.CombatEvent.BECOMES_BLOCKED, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever one or more creatures you control become blocked". */
    public static CombatTrigger creaturesYouControlBecomeBlocked() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ONE_OR_MORE_BECOME_BLOCKED, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a creature becomes blocked". */
    public static CombatTrigger creatureBecomesBlocked() {
        return new CombatTrigger(CombatTrigger.CombatEvent.BECOMES_BLOCKED, ObjectRelation.ANY);
    }

    /**
     * "Whenever this creature deals combat damage to a player"; on an Aura or Equipment, the creature it is attached
     * to; on a Curse, "whenever a creature deals combat damage to enchanted player".
     */
    public static CombatTrigger dealsCombatDamageToPlayer() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_PLAYER, ObjectRelation.THIS);
    }

    /** "Whenever equipped creature deals combat damage to a player", once each combat damage step. */
    public static CombatTrigger equippedCreatureDealsCombatDamageToPlayer() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_PLAYER, ObjectRelation.EQUIPPED);
    }

    /** "Whenever a creature you control deals combat damage to a player". */
    public static CombatTrigger creatureYouControlDealsCombatDamageToPlayer() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_PLAYER, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a creature deals combat damage to a player". */
    public static CombatTrigger creatureDealsCombatDamageToPlayer() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_PLAYER, ObjectRelation.ANY);
    }

    /** "Whenever a creature with the same name as this deals combat damage to a player". */
    public static CombatTrigger creatureWithSameNameDealsCombatDamageToPlayer() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_PLAYER, ObjectRelation.ANY,
                new PermanentHasSameNameAsSourcePredicate());
    }

    /** "Whenever a creature deals combat damage to one of your opponents". */
    public static CombatTrigger creatureDealsCombatDamageToOpponent() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_OPPONENT, ObjectRelation.ANY);
    }

    /** "Whenever one or more goaded creatures deal combat damage to one of your opponents". */
    public static CombatTrigger goadedCreaturesDealCombatDamageToOpponent() {
        return new CombatTrigger(CombatTrigger.CombatEvent.ONE_OR_MORE_COMBAT_DAMAGE_TO_OPPONENT, ObjectRelation.ANY,
                new PermanentIsGoadedPredicate());
    }

    /** "Whenever a creature deals combat damage to its owner". */
    public static CombatTrigger creatureDealsCombatDamageToItsOwner() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_OWNER, ObjectRelation.ANY);
    }

    /**
     * "Whenever this creature deals combat damage to a creature", once for each creature. The stack entry's event value
     * is the excess damage dealt to that creature in that combat damage step ("that excess damage"; {@code EventValue}),
     * against which an intervening "if" such as {@code EventValueAtLeast(1)} ("if that creature was dealt excess
     * damage") is judged as the ability triggers.
     */
    public static CombatTrigger dealsCombatDamageToCreature() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_CREATURE, ObjectRelation.THIS);
    }

    /** "Whenever this creature deals combat damage to one or more blocking creatures". */
    public static CombatTrigger dealsCombatDamageToBlockingCreature() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_BLOCKING_CREATURE, ObjectRelation.THIS);
    }

    /** "Whenever this creature deals combat damage", once each combat damage step. */
    public static CombatTrigger dealsCombatDamage() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE, ObjectRelation.THIS);
    }

    /** "Whenever equipped creature deals combat damage", once each combat damage step. */
    public static CombatTrigger equippedCreatureDealsCombatDamage() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE, ObjectRelation.EQUIPPED);
    }

    /** "Whenever a creature you control deals combat damage", once for each creature each combat damage step. */
    public static CombatTrigger creatureYouControlDealsCombatDamage() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever this creature deals combat damage to a player or planeswalker". */
    public static CombatTrigger dealsCombatDamageToPlayerOrPlaneswalker() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER,
                ObjectRelation.THIS);
    }

    /** "Whenever a creature you control deals combat damage to a player or planeswalker". */
    public static CombatTrigger creatureYouControlDealsCombatDamageToPlayerOrPlaneswalker() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER,
                ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever this creature deals combat damage to a player or battle". */
    public static CombatTrigger dealsCombatDamageToPlayerOrBattle() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE, ObjectRelation.THIS);
    }

    /** "Whenever equipped creature deals combat damage to a player or battle". */
    public static CombatTrigger equippedCreatureDealsCombatDamageToPlayerOrBattle() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE, ObjectRelation.EQUIPPED);
    }

    /** "Whenever a creature you control deals combat damage to a player or battle". */
    public static CombatTrigger creatureYouControlDealsCombatDamageToPlayerOrBattle() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE,
                ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a creature deals combat damage to you". */
    public static CombatTrigger creatureDealsCombatDamageToYou() {
        return new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_YOU, ObjectRelation.ANY);
    }

    /** "Whenever this permanent is dealt combat damage". */
    public static CombatTrigger dealtCombatDamage() {
        return new CombatTrigger(CombatTrigger.CombatEvent.DEALT_COMBAT_DAMAGE, ObjectRelation.THIS);
    }

    /** "Whenever combat damage is dealt to you". */
    public static CombatTrigger youAreDealtCombatDamage() {
        return new CombatTrigger(CombatTrigger.CombatEvent.YOU_DEALT_COMBAT_DAMAGE, ObjectRelation.ANY);
    }

    /** "Whenever combat damage is dealt to you or a planeswalker you control". */
    public static CombatTrigger youOrYourPlaneswalkerDealtCombatDamage() {
        return new CombatTrigger(CombatTrigger.CombatEvent.YOU_OR_YOUR_PLANESWALKER_DEALT_COMBAT_DAMAGE,
                ObjectRelation.ANY);
    }

    /** "Whenever this deals damage to a player"; on an Aura or Equipment, the attached creature. */
    public static DamageTrigger dealsDamageToPlayer() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE_TO_PLAYER, ObjectRelation.THIS);
    }

    /** "Whenever this deals damage to an opponent"; on an Aura or Equipment, the attached creature. */
    public static DamageTrigger dealsDamageToOpponent() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE_TO_OPPONENT, ObjectRelation.THIS);
    }

    /** "Whenever a permanent deals damage to you". */
    public static DamageTrigger permanentDealsDamageToYou() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE_TO_YOU, ObjectRelation.ANY);
    }

    /** "Whenever enchanted creature is dealt damage". */
    public static DamageTrigger enchantedCreatureDealtDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALT_DAMAGE, ObjectRelation.ENCHANTED);
    }

    /** "Whenever enchanted player is dealt damage". */
    public static DamageTrigger enchantedPlayerDealtDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.ENCHANTED_PLAYER_DEALT_DAMAGE, ObjectRelation.ANY);
    }

    /** "Whenever enchanted creature deals damage to you". */
    public static DamageTrigger enchantedCreatureDealsDamageToYou() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE_TO_YOU, ObjectRelation.ENCHANTED);
    }

    /** "Whenever enchanted creature deals damage". */
    public static DamageTrigger enchantedCreatureDealsDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE, ObjectRelation.ENCHANTED);
    }

    /** "Whenever enchanted creature deals damage to a creature". */
    public static DamageTrigger enchantedCreatureDealsDamageToCreature() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE_TO_CREATURE, ObjectRelation.ENCHANTED);
    }

    /** "Whenever this permanent is dealt damage". */
    public static DamageTrigger dealtDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALT_DAMAGE, ObjectRelation.THIS);
    }

    /** "Whenever this permanent is dealt noncombat damage". */
    public static DamageTrigger dealtNoncombatDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALT_NONCOMBAT_DAMAGE, ObjectRelation.THIS);
    }

    /** "Whenever an opponent is dealt noncombat damage". */
    public static DamageTrigger opponentDealtNoncombatDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.OPPONENT_DEALT_NONCOMBAT_DAMAGE, ObjectRelation.ANY);
    }

    /** "Whenever a creature an opponent controls is dealt damage". */
    public static DamageTrigger creatureOpponentControlsDealtDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.CREATURE_DEALT_DAMAGE, ObjectRelation.OPPONENT_CONTROLS);
    }

    /** "Whenever a creature or planeswalker an opponent controls is dealt excess damage". */
    public static DamageTrigger creatureOrPlaneswalkerOpponentControlsDealtExcessDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.CREATURE_OR_PLANESWALKER_DEALT_EXCESS_DAMAGE,
                ObjectRelation.OPPONENT_CONTROLS);
    }

    /** "Whenever a creature or planeswalker an opponent controls is dealt excess noncombat damage". */
    public static DamageTrigger creatureOrPlaneswalkerOpponentControlsDealtExcessNoncombatDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.CREATURE_OR_PLANESWALKER_DEALT_EXCESS_NONCOMBAT_DAMAGE,
                ObjectRelation.OPPONENT_CONTROLS);
    }

    /** "Whenever one or more creatures your opponents control are dealt excess noncombat damage". */
    public static DamageTrigger creaturesOpponentsControlDealtExcessNoncombatDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.ONE_OR_MORE_CREATURES_DEALT_EXCESS_NONCOMBAT_DAMAGE,
                ObjectRelation.OPPONENT_CONTROLS);
    }

    /** "Whenever a creature is dealt damage". */
    public static DamageTrigger creatureDealtDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.CREATURE_DEALT_DAMAGE, ObjectRelation.ANY);
    }

    /**
     * "Whenever a permanent you control is dealt damage"; the effect can narrow it ("a Dragon you control", "this or a
     * creature it's paired with").
     */
    public static DamageTrigger permanentYouControlDealtDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALT_DAMAGE, ObjectRelation.YOU_CONTROL);
    }

    /**
     * "Whenever a creature you control deals damage to a creature", the creature that deals it the trigger's filter
     * ("an Archer you control"); {@link DamageTrigger#withDamagedFilter} narrows the damaged creature and
     * {@link DamageTrigger#withCombatDamageOnly()} the damage.
     */
    public static DamageTrigger creatureYouControlDealsDamageToCreature() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE_TO_CREATURE, ObjectRelation.YOU_CONTROL);
    }

    /**
     * "Whenever this creature deals damage to a creature"; {@link DamageTrigger#withDamagedFilter} narrows the
     * damaged creature ("to a Vampire") and {@link DamageTrigger#withCombatDamageOnly()} the damage.
     */
    public static DamageTrigger dealsDamageToCreature() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE_TO_CREATURE, ObjectRelation.THIS);
    }

    /**
     * "Whenever equipped creature deals damage to a creature", on an Equipment: the creature it is attached to, or was
     * attached to as that creature left the battlefield with the same damage.
     */
    public static DamageTrigger equippedCreatureDealsDamageToCreature() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE_TO_CREATURE, ObjectRelation.EQUIPPED);
    }

    /**
     * "Whenever a creature deals damage to a creature", whoever controls it, the creature that deals it the
     * trigger's filter ("whenever a Sliver deals combat damage to a creature").
     */
    public static DamageTrigger creatureDealsDamageToCreature() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE_TO_CREATURE, ObjectRelation.ANY);
    }

    /**
     * "Whenever an opponent is dealt noncombat damage by a red instant or sorcery spell you control or by a red
     * planeswalker you control".
     */
    public static DamageTrigger opponentDealtDamageByRedInstantSorceryOrPlaneswalkerYouControl() {
        return new DamageTrigger(
                DamageTrigger.DamageEvent.OPPONENT_DEALT_NONCOMBAT_DAMAGE_BY_RED_INSTANT_SORCERY_OR_PLANESWALKER,
                ObjectRelation.YOU_CONTROL);
    }

    /**
     * "Whenever an instant or sorcery spell you control deals damage to an opponent or a battle".
     */
    public static DamageTrigger instantOrSorceryYouControlDealsDamageToOpponentOrBattle() {
        return new DamageTrigger(DamageTrigger.DamageEvent.INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_OPPONENT_OR_BATTLE,
                ObjectRelation.YOU_CONTROL);
    }

    /**
     * "Whenever an instant or sorcery spell you control deals damage to an opponent": once for each opponent dealt
     * damage, "that player" being that opponent and "that much" the damage dealt to them.
     */
    public static DamageTrigger instantOrSorceryYouControlDealsDamageToOpponent() {
        return new DamageTrigger(DamageTrigger.DamageEvent.INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_OPPONENT,
                ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a source you control deals noncombat damage to an opponent". */
    public static DamageTrigger sourceYouControlDealsNoncombatDamageToOpponent() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT, ObjectRelation.YOU_CONTROL);
    }

    /**
     * "Whenever a source deals damage", whoever controls it; {@code withFilter} narrows the source as a permanent and
     * {@code withSpellFilter} as a spell or card ("a red creature or spell", "a Sliver").
     */
    public static DamageTrigger sourceDealsDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE, ObjectRelation.ANY);
    }

    /**
     * "Whenever a source you control deals damage"; filtered as {@link #sourceDealsDamage()} is ("a red source you
     * control", "a noncreature source you control").
     */
    public static DamageTrigger sourceYouControlDealsDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever an instant or sorcery spell you control deals damage". */
    public static DamageTrigger instantOrSorceryYouControlDealsDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.INSTANT_OR_SORCERY_DEALS_DAMAGE, ObjectRelation.YOU_CONTROL);
    }

    /**
     * "Whenever an instant or sorcery spell you control that targets only a single creature deals damage to that
     * creature" (Imodane): that creature is the stack entry's triggering permanent, "that much" its event value.
     */
    public static DamageTrigger instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature() {
        return new DamageTrigger(DamageTrigger.DamageEvent.INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_ITS_ONLY_TARGET_CREATURE,
                ObjectRelation.YOU_CONTROL);
    }

    /**
     * "Whenever a source deals damage to you", once for each source, the damage dealt to you the event's value;
     * {@code withFilter} narrows the source as a permanent and {@code withSpellFilter} as a spell or card ("a source of
     * the chosen color").
     */
    public static DamageTrigger sourceDealsDamageToYou() {
        return new DamageTrigger(DamageTrigger.DamageEvent.SOURCE_DEALS_DAMAGE_TO_YOU, ObjectRelation.ANY);
    }

    /** "Whenever this deals damage". */
    public static DamageTrigger dealsDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE, ObjectRelation.THIS);
    }

    /** "Whenever a creature you control deals damage to a planeswalker". */
    public static DamageTrigger creatureYouControlDealsDamageToPlaneswalker() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE_TO_PLANESWALKER, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a creature you control deals combat damage to a planeswalker". */
    public static DamageTrigger creatureYouControlDealsCombatDamageToPlaneswalker() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_COMBAT_DAMAGE_TO_PLANESWALKER,
                ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a creature deals damage to enchanted planeswalker". */
    public static DamageTrigger creatureDealsDamageToEnchantedPlaneswalker() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE_TO_ENCHANTED_PLANESWALKER, ObjectRelation.ANY);
    }

    /** "Whenever you're dealt damage". */
    public static DamageTrigger youAreDealtDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.YOU_DEALT_DAMAGE, ObjectRelation.ANY);
    }

    /** "Whenever a source you control deals damage to you". */
    public static DamageTrigger youAreDealtDamageBySourceYouControl() {
        return new DamageTrigger(DamageTrigger.DamageEvent.YOU_DEALT_DAMAGE, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever damage that would be dealt to you is prevented". */
    public static DamageTrigger damageToYouPrevented() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DAMAGE_TO_YOU_PREVENTED, ObjectRelation.ANY);
    }

    /** "Whenever an opponent is dealt damage". */
    public static DamageTrigger opponentDealtDamage() {
        return new DamageTrigger(DamageTrigger.DamageEvent.OPPONENT_DEALT_DAMAGE, ObjectRelation.ANY);
    }

    /** "Whenever a source you control deals damage to an opponent". */
    public static DamageTrigger sourceYouControlDealsDamageToOpponent() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE_TO_OPPONENT, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a source you control deals damage to a player". */
    public static DamageTrigger sourceYouControlDealsDamageToPlayer() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE_TO_PLAYER, ObjectRelation.YOU_CONTROL);
    }

    /**
     * "Whenever a creature deals damage to its controller", on a plane: a creature whose controller isn't the planar
     * controller.
     */
    public static DamageTrigger creatureDealsDamageToItsController() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE_TO_ITS_CONTROLLER, ObjectRelation.ANY);
    }

    /** "Whenever one or more creatures you control deal damage to a player". */
    public static DamageTrigger creaturesYouControlDealDamageToPlayer() {
        return new DamageTrigger(DamageTrigger.DamageEvent.ONE_OR_MORE_DEAL_DAMAGE_TO_PLAYER,
                ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever one or more creatures you control deal damage to an opponent". */
    public static DamageTrigger creaturesYouControlDealDamageToOpponent() {
        return new DamageTrigger(DamageTrigger.DamageEvent.ONE_OR_MORE_DEAL_DAMAGE_TO_OPPONENT,
                ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a source an opponent controls deals damage to you". */
    public static DamageTrigger youAreDealtDamageBySourceOpponentControls() {
        return new DamageTrigger(DamageTrigger.DamageEvent.YOU_DEALT_DAMAGE, ObjectRelation.OPPONENT_CONTROLS);
    }

    /** "Whenever a source an opponent controls deals damage to you or a permanent you control". */
    public static DamageTrigger sourceOpponentControlsDealsDamageToYouOrYourPermanent() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT,
                ObjectRelation.OPPONENT_CONTROLS);
    }

    /**
     * "Whenever a creature deals damage to you or a permanent you control"; the effect filters the creature ("of the
     * chosen color").
     */
    public static DamageTrigger creatureDealsDamageToYouOrYourPermanent() {
        return new DamageTrigger(DamageTrigger.DamageEvent.CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT,
                ObjectRelation.ANY);
    }

    /** "Whenever a source you control deals noncombat damage to a creature". */
    public static DamageTrigger sourceYouControlDealsNoncombatDamageToCreature() {
        return new DamageTrigger(DamageTrigger.DamageEvent.DEALS_NONCOMBAT_DAMAGE_TO_CREATURE,
                ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a time counter is removed from this card while it's exiled", from exile. */
    public static CounterTrigger timeCounterRemovedFromThisInExile() {
        return new CounterTrigger(CounterTrigger.CounterEvent.TIME_COUNTER_REMOVED_IN_EXILE, ObjectRelation.THIS);
    }

    /** "Whenever you remove a time counter from this card while it's exiled", from exile. */
    public static CounterTrigger youRemoveTimeCounterFromThisInExile() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_REMOVE_TIME_COUNTER_IN_EXILE, ObjectRelation.THIS);
    }

    /** "When this creature becomes renowned" */
    public static CounterTrigger becomesRenowned() {
        return new CounterTrigger(CounterTrigger.CounterEvent.BECOMES_RENOWNED, ObjectRelation.THIS);
    }

    /** "Whenever a creature you control becomes renowned" */
    public static CounterTrigger creatureYouControlBecomesRenowned() {
        return new CounterTrigger(CounterTrigger.CounterEvent.BECOMES_RENOWNED, ObjectRelation.YOU_CONTROL, CREATURE);
    }

    /** "When this creature becomes monstrous" */
    public static CounterTrigger becomesMonstrous() {
        return new CounterTrigger(CounterTrigger.CounterEvent.BECOMES_MONSTROUS, ObjectRelation.THIS);
    }

    /** "Whenever you get one or more {E}" */
    public static CounterTrigger youGetEnergy() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_GET_ENERGY, ObjectRelation.ANY);
    }

    /** "Whenever you put one or more counters on a creature you don't control" */
    public static CounterTrigger youPutCountersOnCreatureYouDontControl() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_COUNTERS,
                ObjectRelation.OPPONENT_CONTROLS, CREATURE);
    }

    /** "Whenever you put one or more counters on a creature" */
    public static CounterTrigger youPutCountersOnCreature() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_COUNTERS, ObjectRelation.ANY, CREATURE);
    }

    /** "Whenever one or more +1/+1 counters are put on this permanent" */
    public static CounterTrigger plusOneCountersPutOnThis() {
        return new CounterTrigger(CounterTrigger.CounterEvent.PLUS_ONE_COUNTERS_PUT, ObjectRelation.THIS);
    }

    /** "Whenever a +1/+1 counter is put on this permanent", once for each counter. */
    public static CounterTrigger plusOneCounterPutOnThis() {
        return new CounterTrigger(CounterTrigger.CounterEvent.EACH_PLUS_ONE_COUNTER_PUT, ObjectRelation.THIS);
    }

    /** "Whenever you put one or more +1/+1 counters on this permanent" */
    public static CounterTrigger youPutPlusOneCountersOnThis() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_PLUS_ONE_COUNTERS, ObjectRelation.THIS);
    }

    /** "Whenever this creature trains" */
    public static CounterTrigger trains() {
        return new CounterTrigger(CounterTrigger.CounterEvent.TRAINS, ObjectRelation.THIS);
    }

    /** "Whenever one or more +1/+1 counters are put on a Human you control" */
    public static CounterTrigger plusOneCountersPutOnHumanYouControl() {
        return new CounterTrigger(CounterTrigger.CounterEvent.PLUS_ONE_COUNTERS_PUT,
                ObjectRelation.YOU_CONTROL, HUMAN_CREATURE);
    }

    /** "When this Class becomes level 2" */
    public static CounterTrigger reachesLevelTwo() {
        return new CounterTrigger(CounterTrigger.CounterEvent.REACHES_LEVEL_TWO, ObjectRelation.THIS);
    }

    /** "When this Class becomes level 3" */
    public static CounterTrigger reachesLevelThree() {
        return new CounterTrigger(CounterTrigger.CounterEvent.REACHES_LEVEL_THREE, ObjectRelation.THIS);
    }

    /** "Whenever one or more counters are put on this permanent" */
    public static CounterTrigger countersPutOnThis() {
        return new CounterTrigger(CounterTrigger.CounterEvent.COUNTERS_PUT, ObjectRelation.THIS);
    }

    /** "Whenever one or more +1/+1 counters are put on another non-Hydra creature you control" */
    public static CounterTrigger plusOneCountersPutOnAnotherNonHydraCreatureYouControl() {
        return new CounterTrigger(CounterTrigger.CounterEvent.PLUS_ONE_COUNTERS_PUT,
                ObjectRelation.ANOTHER_YOU_CONTROL, NON_HYDRA_CREATURE);
    }

    /** "Whenever one or more +1/+1 counters are put on another creature you control" */
    public static CounterTrigger plusOneCountersPutOnAnotherCreatureYouControl() {
        return new CounterTrigger(CounterTrigger.CounterEvent.PLUS_ONE_COUNTERS_PUT,
                ObjectRelation.ANOTHER_YOU_CONTROL, CREATURE);
    }

    /**
     * "Whenever you put one or more +1/+1 counters on another permanent you control, if it's the first time +1/+1
     * counters have been put on that permanent this turn"
     */
    public static CounterTrigger plusOneCountersPutOnAnotherPermanentYouControlFirstTimeEachTurn() {
        return new CounterTrigger(CounterTrigger.CounterEvent.FIRST_PLUS_ONE_COUNTERS_PUT_THIS_TURN,
                ObjectRelation.ANOTHER_YOU_CONTROL);
    }

    /** "Whenever you put one or more +1/+1 counters on a permanent you control" */
    public static CounterTrigger youPutPlusOneCountersOnPermanentYouControl() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_PLUS_ONE_COUNTERS, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever you put one or more +1/+1 counters on a creature you control" */
    public static CounterTrigger youPutPlusOneCountersOnCreatureYouControl() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_PLUS_ONE_COUNTERS,
                ObjectRelation.YOU_CONTROL, CREATURE);
    }

    /** "Whenever one or more +1/+1 counters are put on a creature you control" */
    public static CounterTrigger plusOneCountersPutOnCreatureYouControl() {
        return new CounterTrigger(CounterTrigger.CounterEvent.PLUS_ONE_COUNTERS_PUT,
                ObjectRelation.YOU_CONTROL, CREATURE);
    }

    /** "Whenever you put one or more +1/+1 counters on a creature" */
    public static CounterTrigger youPutPlusOneCountersOnCreature() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_PLUS_ONE_COUNTERS, ObjectRelation.ANY, CREATURE);
    }

    /** "Whenever you put one or more +1/+1 counters on other Heroes you control" */
    public static CounterTrigger youPutPlusOneCountersOnAnotherHeroYouControl() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_PLUS_ONE_COUNTERS,
                ObjectRelation.ANOTHER_YOU_CONTROL, HERO_CREATURE);
    }

    /** "Whenever you put one or more +1/+1 counters on another creature" */
    public static CounterTrigger youPutPlusOneCountersOnAnotherCreature() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_PLUS_ONE_COUNTERS,
                ObjectRelation.ANOTHER, CREATURE);
    }

    /** "Whenever you put one or more counters on a permanent or player" */
    public static CounterTrigger youPutCountersOnPermanentOrPlayer() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER,
                ObjectRelation.ANY);
    }

    /** "Whenever you put one or more time counters on a permanent you control" */
    public static CounterTrigger youPutTimeCountersOnPermanentYouControl() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_TIME_COUNTERS, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever you put one or more counters on another creature" */
    public static CounterTrigger youPutCountersOnAnotherCreature() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_COUNTERS, ObjectRelation.ANOTHER, CREATURE);
    }

    /** "Whenever an opponent puts one or more counters on a creature they control" */
    public static CounterTrigger opponentPutsCountersOnCreatureTheyControl() {
        return new CounterTrigger(CounterTrigger.CounterEvent.OPPONENT_PUTS_COUNTERS_ON_OWN,
                ObjectRelation.OPPONENT_CONTROLS, CREATURE);
    }

    /** "Whenever you put a lore counter on a Saga you control", once for each counter. */
    public static CounterTrigger youPutLoreCounterOnSagaYouControl() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_EACH_LORE_COUNTER,
                ObjectRelation.YOU_CONTROL, SAGA);
    }

    /** "Whenever one or more counters are put on a creature you control" */
    public static CounterTrigger countersPutOnCreatureYouControl() {
        return new CounterTrigger(CounterTrigger.CounterEvent.COUNTERS_PUT, ObjectRelation.YOU_CONTROL, CREATURE);
    }

    /** "Whenever you put one or more counters on a creature you control" */
    public static CounterTrigger youPutCountersOnCreatureYouControl() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_COUNTERS, ObjectRelation.YOU_CONTROL, CREATURE);
    }

    /** "Whenever this creature evolves" */
    public static CounterTrigger evolves() {
        return new CounterTrigger(CounterTrigger.CounterEvent.EVOLVES, ObjectRelation.THIS);
    }

    /** "Whenever one or more loyalty counters are removed from this planeswalker" */
    public static CounterTrigger loyaltyCountersRemovedFromThis() {
        return new CounterTrigger(CounterTrigger.CounterEvent.LOYALTY_COUNTERS_REMOVED, ObjectRelation.THIS);
    }

    /** "Whenever one or more time counters are removed from this permanent" */
    public static CounterTrigger timeCountersRemovedFromThis() {
        return new CounterTrigger(CounterTrigger.CounterEvent.TIME_COUNTERS_REMOVED, ObjectRelation.THIS);
    }

    /** "Whenever a +1/+1 counter is removed from this creature", once for each counter. */
    public static CounterTrigger plusOneCounterRemovedFromThis() {
        return new CounterTrigger(CounterTrigger.CounterEvent.EACH_PLUS_ONE_COUNTER_REMOVED, ObjectRelation.THIS);
    }

    /** "Whenever one or more counters are removed from a permanent you control" */
    public static CounterTrigger countersRemovedFromPermanentYouControl() {
        return new CounterTrigger(CounterTrigger.CounterEvent.COUNTERS_REMOVED, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever one or more loyalty counters are put on planeswalkers you control" */
    public static CounterTrigger loyaltyCountersPutOnPlaneswalkersYouControl() {
        return new CounterTrigger(CounterTrigger.CounterEvent.LOYALTY_COUNTERS_PUT,
                ObjectRelation.YOU_CONTROL, PLANESWALKER);
    }

    /** "Whenever you put one or more -1/-1 counters on this creature" */
    public static CounterTrigger youPutMinusOneCountersOnThis() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_MINUS_ONE_COUNTERS, ObjectRelation.THIS);
    }

    /** "Whenever a -1/-1 counter is put on a creature", once for each counter. */
    public static CounterTrigger minusOneCounterPutOnCreature() {
        return new CounterTrigger(CounterTrigger.CounterEvent.EACH_MINUS_ONE_COUNTER_PUT, ObjectRelation.ANY, CREATURE);
    }

    /** "Whenever one or more -1/-1 counters are put on a creature" */
    public static CounterTrigger minusOneCountersPutOnCreature() {
        return new CounterTrigger(CounterTrigger.CounterEvent.MINUS_ONE_COUNTERS_PUT, ObjectRelation.ANY, CREATURE);
    }

    /** "Whenever you put a -1/-1 counter on a creature", once for each counter. */
    public static CounterTrigger youPutMinusOneCounterOnCreature() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_EACH_MINUS_ONE_COUNTER,
                ObjectRelation.ANY, CREATURE);
    }

    /** "Whenever you put one or more -1/-1 counters on a creature" */
    public static CounterTrigger youPutMinusOneCountersOnCreature() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PUT_MINUS_ONE_COUNTERS, ObjectRelation.ANY, CREATURE);
    }

    /** "Whenever you proliferate" */
    public static CounterTrigger youProliferate() {
        return new CounterTrigger(CounterTrigger.CounterEvent.YOU_PROLIFERATE, ObjectRelation.ANY);
    }

    /** "Whenever a player chooses one or more targets" for a spell or ability */
    public static TargetTrigger playerChoosesTargets() {
        return new TargetTrigger(TargetTrigger.TargetEvent.PLAYER_CHOOSES_TARGETS, ObjectRelation.ANY);
    }

    /** "Whenever a player or permanent becomes the target of an ability you control" */
    public static TargetTrigger playerOrPermanentBecomesTargetOfYourAbility() {
        return new TargetTrigger(TargetTrigger.TargetEvent.PLAYER_OR_PERMANENT_BECOMES_TARGET_OF_YOUR_ABILITY,
                ObjectRelation.ANY);
    }

    /** "Whenever this permanent becomes the target of a spell" */
    public static TargetTrigger becomesTargetOfSpell() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_SPELL, ObjectRelation.THIS);
    }

    /** "Whenever this permanent becomes the target of a spell you control" */
    public static TargetTrigger becomesTargetOfYourSpell() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_YOUR_SPELL, ObjectRelation.THIS);
    }

    /** "Whenever this permanent becomes the target of an Aura spell" */
    public static TargetTrigger becomesTargetOfAuraSpell() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_AURA_SPELL, ObjectRelation.THIS);
    }

    /** "Whenever this permanent becomes the target of a spell or ability an opponent controls": ward's trigger. */
    public static TargetTrigger becomesTargetOfOpponentSpellOrAbility() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                ObjectRelation.THIS);
    }

    /** "Whenever this permanent becomes the target of a spell an opponent controls" */
    public static TargetTrigger becomesTargetOfOpponentSpell() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_OPPONENT_SPELL, ObjectRelation.THIS);
    }

    /** "Whenever you become the target of a spell" */
    public static TargetTrigger youBecomeTargetOfSpell() {
        return new TargetTrigger(TargetTrigger.TargetEvent.YOU_BECOME_TARGET_OF_SPELL, ObjectRelation.ANY);
    }

    /** "Whenever you become the target of a spell or ability an opponent controls" */
    public static TargetTrigger youBecomeTargetOfOpponentSpellOrAbility() {
        return new TargetTrigger(TargetTrigger.TargetEvent.YOU_BECOME_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                ObjectRelation.ANY);
    }

    /** "Whenever this permanent becomes the target of a spell or ability" */
    public static TargetTrigger becomesTargetOfSpellOrAbility() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_SPELL_OR_ABILITY, ObjectRelation.THIS);
    }

    /** "Whenever this permanent becomes the target of a spell or ability you control" (valiant) */
    public static TargetTrigger becomesTargetOfYourSpellOrAbility() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY,
                ObjectRelation.THIS);
    }

    /** "Whenever a creature you control becomes the target of a spell or ability an opponent controls" */
    public static TargetTrigger creatureYouControlBecomesTargetOfOpponentSpellOrAbility() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                ObjectRelation.YOU_CONTROL, CREATURE);
    }

    /** "Whenever another creature you control becomes the target of a spell or ability" */
    public static TargetTrigger anotherCreatureYouControlBecomesTargetOfSpellOrAbility() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                ObjectRelation.ANOTHER_YOU_CONTROL, CREATURE);
    }

    /** "Whenever one or more creatures you control become the target of an activated ability", once for each ability */
    public static TargetTrigger creaturesYouControlBecomeTargetOfActivatedAbility() {
        return new TargetTrigger(TargetTrigger.TargetEvent.ONE_OR_MORE_BECOME_TARGET_OF_ACTIVATED_ABILITY,
                ObjectRelation.YOU_CONTROL, CREATURE);
    }

    /** "Whenever a creature you control becomes the target of a spell an opponent controls" */
    public static TargetTrigger creatureYouControlBecomesTargetOfOpponentSpell() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_OPPONENT_SPELL,
                ObjectRelation.YOU_CONTROL, CREATURE);
    }

    /**
     * "Whenever a creature you control or a creature spell you control becomes the target of a spell or ability an
     * opponent controls"
     */
    public static TargetTrigger creatureOrCreatureSpellYouControlBecomesTargetOfOpponentSpellOrAbility() {
        return new TargetTrigger(
                TargetTrigger.TargetEvent.PERMANENT_OR_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                ObjectRelation.YOU_CONTROL, CREATURE);
    }

    /** "Whenever you or a permanent you control becomes the target of a spell or ability an opponent controls" */
    public static TargetTrigger youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility() {
        return new TargetTrigger(TargetTrigger.TargetEvent.YOU_OR_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a permanent you control becomes the target of a spell or ability an opponent controls" */
    public static TargetTrigger permanentYouControlBecomesTargetOfOpponentSpellOrAbility() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever another permanent you control becomes the target of a spell or ability an opponent controls" */
    public static TargetTrigger anotherPermanentYouControlBecomesTargetOfOpponentSpellOrAbility() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                ObjectRelation.ANOTHER_YOU_CONTROL);
    }

    /** "Whenever a creature becomes the target of a spell or ability" */
    public static TargetTrigger creatureBecomesTargetOfSpellOrAbility() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                ObjectRelation.ANY, CREATURE);
    }

    /** "Whenever a creature an opponent controls becomes the target of a spell or ability you control" */
    public static TargetTrigger creatureOpponentControlsBecomesTargetOfYourSpellOrAbility() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY,
                ObjectRelation.OPPONENT_CONTROLS, CREATURE);
    }

    /** "Whenever a creature you control becomes the target of a spell" */
    public static TargetTrigger creatureYouControlBecomesTargetOfSpell() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_SPELL,
                ObjectRelation.YOU_CONTROL, CREATURE);
    }

    /** "Whenever a creature you control becomes the target of a backup ability" */
    public static TargetTrigger creatureYouControlBecomesTargetOfBackupAbility() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_BACKUP_ABILITY,
                ObjectRelation.YOU_CONTROL, CREATURE);
    }

    /** "Whenever a creature you control becomes the target of an instant or sorcery spell" */
    public static TargetTrigger creatureYouControlBecomesTargetOfInstantOrSorcerySpell() {
        return new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_INSTANT_OR_SORCERY_SPELL,
                ObjectRelation.YOU_CONTROL, CREATURE);
    }

    /** "Whenever you draw a card", once for each card */
    public static LibraryTrigger youDrawCard() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.DRAWS, PlayerRelation.YOU);
    }

    /** "Whenever you draw your second card each turn" */
    public static LibraryTrigger youDrawSecondCardEachTurn() {
        return youDrawNthCardEachTurn(2);
    }

    /** "Whenever you draw your {@code nth} card each turn": "When you draw your third card in a turn" is 3 */
    public static LibraryTrigger youDrawNthCardEachTurn(int nth) {
        return youDrawCard().nthTimeEachTurn(nth);
    }

    /** "Whenever you draw your first card during each of your draw steps" */
    public static LibraryTrigger youDrawFirstCardInDrawStep() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.DRAWS_FIRST_IN_DRAW_STEP, PlayerRelation.YOU);
    }

    /** "Whenever an opponent draws their second card each turn" */
    public static LibraryTrigger opponentDrawsSecondCardEachTurn() {
        return opponentDrawsNthCardEachTurn(2);
    }

    /** "Whenever an opponent draws their {@code nth} card each turn": "their first card each turn" is 1 */
    public static LibraryTrigger opponentDrawsNthCardEachTurn(int nth) {
        return opponentDrawsCard().nthTimeEachTurn(nth);
    }

    /** "Whenever a player draws a card", once for each card: read only from an emblem (Ob Nixilis Reignited's) */
    public static LibraryTrigger playerDrawsCard() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.DRAWS, PlayerRelation.ANY);
    }

    /** "Whenever an opponent draws a card", once for each card */
    public static LibraryTrigger opponentDrawsCard() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.DRAWS, PlayerRelation.OPPONENT);
    }

    /** "Whenever an opponent draws a card except the first one they draw in each of their draw steps" */
    public static LibraryTrigger opponentDrawsCardExceptFirstInDrawStep() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.DRAWS_EXCEPT_FIRST_IN_DRAW_STEP,
                PlayerRelation.OPPONENT);
    }

    /** "Whenever enchanted player draws a card", once for each card */
    public static LibraryTrigger enchantedPlayerDrawsCard() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.ENCHANTED_PLAYER_DRAWS, PlayerRelation.ANY);
    }

    /** "Whenever an opponent discards a card", once for each card, cycling included */
    public static LibraryTrigger opponentDiscardsCard() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.DISCARDS, PlayerRelation.OPPONENT);
    }

    /** "Whenever you discard a card", once for each card, cycling included */
    public static LibraryTrigger youDiscardCard() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.DISCARDS, PlayerRelation.YOU);
    }

    /** "Whenever a spell or ability an opponent controls causes you to discard a card", once for each card */
    public static LibraryTrigger opponentCausesYouToDiscardCard() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.DISCARDS_BY_OPPONENT, PlayerRelation.YOU);
    }

    /** "Whenever you discard one or more cards", once for each discard event */
    public static LibraryTrigger youDiscardOneOrMoreCards() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.DISCARDS_ONE_OR_MORE, PlayerRelation.YOU);
    }

    /** "Whenever a player cycles a card" */
    public static LibraryTrigger playerCyclesCard() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.CYCLES, PlayerRelation.ANY);
    }

    /** "Whenever you cycle a card" (or "another card", when the source can't be cycled from where it is) */
    public static LibraryTrigger youCycleCard() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.CYCLES, PlayerRelation.YOU);
    }

    /** "When you cycle this card" */
    public static LibraryTrigger youCycleThisCard() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.THIS_CYCLED, PlayerRelation.YOU);
    }

    /** "When you discard this card", whatever causes the discard */
    public static LibraryTrigger youDiscardThisCard() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.THIS_DISCARDED, PlayerRelation.ANY);
    }

    /** "When a spell or ability an opponent controls causes you to discard this card" */
    public static LibraryTrigger opponentCausesYouToDiscardThisCard() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.THIS_DISCARDED, PlayerRelation.OPPONENT);
    }

    /** "Whenever you scry" */
    public static LibraryTrigger youScry() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.SCRIES, PlayerRelation.YOU);
    }

    /** "Whenever an opponent scries" */
    public static LibraryTrigger opponentScries() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.SCRIES, PlayerRelation.OPPONENT);
    }

    /** "Whenever you surveil" */
    public static LibraryTrigger youSurveil() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.SURVEILS, PlayerRelation.YOU);
    }

    /** "Whenever an opponent surveils" */
    public static LibraryTrigger opponentSurveils() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.SURVEILS, PlayerRelation.OPPONENT);
    }

    /** "Whenever you search your library" */
    public static LibraryTrigger youSearchYourLibrary() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.SEARCHES_OWN_LIBRARY, PlayerRelation.YOU);
    }

    /** "Whenever an opponent searches their library" */
    public static LibraryTrigger opponentSearchesTheirLibrary() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.SEARCHES_OWN_LIBRARY, PlayerRelation.OPPONENT);
    }

    /** "Whenever an opponent shuffles their library" */
    public static LibraryTrigger opponentShufflesLibrary() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.SHUFFLES_LIBRARY, PlayerRelation.OPPONENT);
    }

    /** "Whenever a player shuffles their library" */
    public static LibraryTrigger playerShufflesLibrary() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.SHUFFLES_LIBRARY, PlayerRelation.ANY);
    }

    /** "Whenever a spell or ability causes its controller to shuffle their library" */
    public static LibraryTrigger spellOrAbilityCausesItsControllerToShuffle() {
        return new LibraryTrigger(LibraryTrigger.LibraryEvent.SPELL_OR_ABILITY_CAUSES_ITS_CONTROLLER_TO_SHUFFLE,
                PlayerRelation.ANY);
    }

    /** "Whenever you tap this permanent for mana" */
    public static TapTrigger tappedForMana() {
        return new TapTrigger(TapTrigger.TapEvent.TAPPED_FOR_MANA, ObjectRelation.THIS);
    }

    /** "Whenever a player taps a land for mana"; the effect narrows the player and the land */
    public static TapTrigger playerTapsLandForMana() {
        return new TapTrigger(TapTrigger.TapEvent.TAPPED_FOR_MANA, ObjectRelation.ANY, LAND);
    }

    /** "Whenever a player taps a permanent for mana" */
    public static TapTrigger playerTapsPermanentForMana() {
        return new TapTrigger(TapTrigger.TapEvent.TAPPED_FOR_MANA, ObjectRelation.ANY);
    }

    /** "Whenever you tap a creature for mana" */
    public static TapTrigger youTapCreatureForMana() {
        return new TapTrigger(TapTrigger.TapEvent.TAPPED_FOR_MANA, ObjectRelation.YOU_CONTROL, CREATURE);
    }

    /** "Whenever you tap a nonland permanent for mana" */
    public static TapTrigger youTapNonlandPermanentForMana() {
        return new TapTrigger(TapTrigger.TapEvent.TAPPED_FOR_MANA, ObjectRelation.YOU_CONTROL, NONLAND);
    }

    /** "Whenever you tap an artifact token for mana" */
    public static TapTrigger youTapArtifactTokenForMana() {
        return new TapTrigger(TapTrigger.TapEvent.TAPPED_FOR_MANA, ObjectRelation.YOU_CONTROL, ARTIFACT_TOKEN);
    }

    /** "Whenever enchanted permanent becomes tapped", also equipped or fortified */
    public static TapTrigger enchantedPermanentBecomesTapped() {
        return new TapTrigger(TapTrigger.TapEvent.BECOMES_TAPPED, ObjectRelation.ENCHANTED);
    }

    /** "Whenever this permanent becomes tapped" */
    public static TapTrigger becomesTapped() {
        return new TapTrigger(TapTrigger.TapEvent.BECOMES_TAPPED, ObjectRelation.THIS);
    }

    /** "Whenever this creature becomes tapped for the first time during each of your turns" */
    public static TapTrigger becomesTappedForFirstTimeDuringYourTurn() {
        return new TapTrigger(TapTrigger.TapEvent.BECOMES_TAPPED_FOR_FIRST_TIME_DURING_YOUR_TURN, ObjectRelation.THIS);
    }

    /**
     * "Whenever a creature you control becomes tapped during your turn, if it's the first time that creature has
     * become tapped this turn"
     */
    public static TapTrigger creatureYouControlBecomesTappedForFirstTimeDuringYourTurn() {
        return new TapTrigger(TapTrigger.TapEvent.BECOMES_TAPPED_FOR_FIRST_TIME_DURING_YOUR_TURN,
                ObjectRelation.YOU_CONTROL, CREATURE);
    }

    /** "Whenever a permanent you control becomes tapped" */
    public static TapTrigger permanentYouControlBecomesTapped() {
        return new TapTrigger(TapTrigger.TapEvent.BECOMES_TAPPED, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a permanent an opponent controls becomes tapped" */
    public static TapTrigger permanentOpponentControlsBecomesTapped() {
        return new TapTrigger(TapTrigger.TapEvent.BECOMES_TAPPED, ObjectRelation.OPPONENT_CONTROLS);
    }

    /** "Whenever a creature becomes tapped" */
    public static TapTrigger creatureBecomesTapped() {
        return new TapTrigger(TapTrigger.TapEvent.BECOMES_TAPPED, ObjectRelation.ANY, CREATURE);
    }

    /** "Whenever you tap a permanent an opponent controls" */
    public static TapTrigger youTapPermanentOpponentControls() {
        return new TapTrigger(TapTrigger.TapEvent.YOU_TAP, ObjectRelation.OPPONENT_CONTROLS);
    }

    /** "Whenever this creature becomes tapped to pay a teamwork cost" */
    public static TapTrigger tappedToPayTeamworkCost() {
        return new TapTrigger(TapTrigger.TapEvent.TAPPED_TO_PAY_TEAMWORK_COST, ObjectRelation.THIS);
    }

    /** "Whenever this permanent becomes untapped" */
    public static TapTrigger becomesUntapped() {
        return new TapTrigger(TapTrigger.TapEvent.BECOMES_UNTAPPED, ObjectRelation.THIS);
    }

    /** "Whenever a permanent you control becomes untapped" */
    public static TapTrigger permanentYouControlBecomesUntapped() {
        return new TapTrigger(TapTrigger.TapEvent.BECOMES_UNTAPPED, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a permanent becomes untapped" */
    public static TapTrigger permanentBecomesUntapped() {
        return new TapTrigger(TapTrigger.TapEvent.BECOMES_UNTAPPED, ObjectRelation.ANY);
    }

    /** "Whenever you untap one or more permanents during your untap step" */
    public static TapTrigger youUntapPermanentsDuringUntapStep() {
        return new TapTrigger(TapTrigger.TapEvent.YOU_UNTAP_DURING_UNTAP_STEP, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever you gain life" */
    public static LifeTrigger youGainLife() {
        return new LifeTrigger(LifeTrigger.LifeEvent.GAINS, PlayerRelation.YOU);
    }

    /** "Whenever an opponent gains life" */
    public static LifeTrigger opponentGainsLife() {
        return new LifeTrigger(LifeTrigger.LifeEvent.GAINS, PlayerRelation.OPPONENT);
    }

    /** "Whenever a player gains life" */
    public static LifeTrigger playerGainsLife() {
        return new LifeTrigger(LifeTrigger.LifeEvent.GAINS, PlayerRelation.ANY);
    }

    /** "Whenever you lose life", damage included */
    public static LifeTrigger youLoseLife() {
        return new LifeTrigger(LifeTrigger.LifeEvent.LOSES, PlayerRelation.YOU);
    }

    /** "Whenever an opponent loses life", damage included */
    public static LifeTrigger opponentLosesLife() {
        return new LifeTrigger(LifeTrigger.LifeEvent.LOSES, PlayerRelation.OPPONENT);
    }

    /** "Whenever an opponent loses life for the first time during each of their turns", damage included */
    public static LifeTrigger opponentLosesLifeForFirstTimeDuringTheirTurn() {
        return new LifeTrigger(LifeTrigger.LifeEvent.LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN, PlayerRelation.OPPONENT);
    }

    /** "Whenever you pay life" */
    public static LifeTrigger youPayLife() {
        return new LifeTrigger(LifeTrigger.LifeEvent.PAYS, PlayerRelation.YOU);
    }

    /**
     * A Saga's chapter ability "{rN}—": when lore counters put on this Saga make their number reach {@code chapter}
     * (CR 714.2b). Every effect added with the same chapter joins one ability.
     */
    public static ChapterTrigger chapter(int chapter) {
        return new ChapterTrigger(chapter);
    }

    /** "Whenever the final chapter ability of a Saga you control triggers" */
    public static FinalChapterTrigger finalChapterAbilityTriggers() {
        return new FinalChapterTrigger(FinalChapterTrigger.FinalChapterEvent.TRIGGERS);
    }

    /** "Whenever the final chapter ability of a Saga you control resolves" */
    public static FinalChapterTrigger finalChapterAbilityResolves() {
        return new FinalChapterTrigger(FinalChapterTrigger.FinalChapterEvent.RESOLVES);
    }

    /** "Whenever you set a scheme in motion" */
    public static PlayerActionTrigger youSetSchemeInMotion() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.SETS_SCHEME_IN_MOTION, PlayerRelation.YOU);
    }

    /** "Whenever you give a gift" */
    public static PlayerActionTrigger youGiveGift() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.GIVES_GIFT, PlayerRelation.YOU);
    }

    /** "Whenever you solve a Case" */
    public static PlayerActionTrigger youSolveCase() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.SOLVES_CASE, PlayerRelation.YOU);
    }

    /** "Whenever you foretell a card" */
    public static PlayerActionTrigger youForetell() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.FORETELLS, PlayerRelation.YOU);
    }

    /** "Whenever you forage" */
    public static PlayerActionTrigger youForage() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.FORAGES, PlayerRelation.YOU);
    }

    /** "Whenever players finish voting" */
    public static PlayerActionTrigger playersFinishVoting() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.FINISH_VOTING, PlayerRelation.ANY);
    }

    /** "Whenever the Ring tempts you" */
    public static PlayerActionTrigger ringTemptsYouAndYouChooseRingBearer() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.CHOOSES_RING_BEARER, PlayerRelation.YOU);
    }

    /** "Whenever you investigate for the first time each turn" */
    public static PlayerActionTrigger youInvestigateFirstTimeEachTurn() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.INVESTIGATES_FIRST_TIME_EACH_TURN,
                PlayerRelation.YOU);
    }

    /** "Whenever you investigate one or more times" */
    public static PlayerActionTrigger youInvestigate() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.INVESTIGATES, PlayerRelation.YOU);
    }

    /** "Whenever you manifest dread" */
    public static PlayerActionTrigger youManifestDread() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.MANIFESTS_DREAD, PlayerRelation.YOU);
    }

    /** "Whenever you complete a dungeon" */
    public static PlayerActionTrigger youCompleteDungeon() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.COMPLETES_DUNGEON, PlayerRelation.YOU);
    }

    /** "Whenever you roll one or more dice" */
    public static PlayerActionTrigger youRollOneOrMoreDice() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.ROLLS_ONE_OR_MORE_DICE, PlayerRelation.YOU);
    }

    /** "Whenever you roll a die", once for each die, with its result as the event value */
    public static PlayerActionTrigger youRollDie() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.ROLLS_DIE, PlayerRelation.YOU);
    }

    /** "Whenever you roll a die's highest natural result", once for each such die */
    public static PlayerActionTrigger youRollHighestNaturalResult() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.ROLLS_HIGHEST_NATURAL_RESULT,
                PlayerRelation.YOU);
    }

    /** "Whenever you roll a natural 20" */
    public static PlayerActionTrigger youRollNatural20() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.ROLLS_NATURAL_20, PlayerRelation.YOU);
    }

    /** "Whenever you waterbend, earthbend, firebend or airbend" */
    public static PlayerActionTrigger youBend() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.BENDS, PlayerRelation.YOU);
    }

    /** "Whenever you exert a creature" */
    public static PlayerActionTrigger youExertCreature() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.EXERTS, PlayerRelation.YOU);
    }

    /** "Whenever you collect evidence" */
    public static PlayerActionTrigger youCollectEvidence() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.COLLECTS_EVIDENCE, PlayerRelation.YOU);
    }

    /** "Whenever you discover" */
    public static PlayerActionTrigger youDiscover() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.DISCOVERS, PlayerRelation.YOU);
    }

    /** "Whenever you seek one or more cards" */
    public static PlayerActionTrigger youSeek() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.SEEKS, PlayerRelation.YOU);
    }

    /** "Whenever you conjure one or more cards" */
    public static PlayerActionTrigger youConjure() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.CONJURES, PlayerRelation.YOU);
    }

    /** "Whenever you become the monarch" */
    public static PlayerActionTrigger youBecomeMonarch() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.BECOMES_MONARCH, PlayerRelation.YOU);
    }

    /** "Whenever an opponent becomes the monarch" */
    public static PlayerActionTrigger opponentBecomesMonarch() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.BECOMES_MONARCH, PlayerRelation.OPPONENT);
    }

    /** "Whenever the Ring tempts you" */
    public static PlayerActionTrigger ringTemptsYou() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.TEMPTED_BY_RING, PlayerRelation.YOU);
    }

    /** "Whenever you win a coin flip" */
    public static PlayerActionTrigger youWinCoinFlip() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.WINS_COIN_FLIP, PlayerRelation.YOU);
    }

    /** "Whenever a player wins a coin flip" */
    public static PlayerActionTrigger playerWinsCoinFlip() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.WINS_COIN_FLIP, PlayerRelation.ANY);
    }

    /** "Whenever you lose a coin flip" */
    public static PlayerActionTrigger youLoseCoinFlip() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.LOSES_COIN_FLIP, PlayerRelation.YOU);
    }

    /** "Whenever an opponent pays a mana tax imposed by a spell or permanent you control" */
    public static PlayerActionTrigger opponentPaysTax() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.PAYS_TAX, PlayerRelation.OPPONENT);
    }

    /** "Whenever you clash" */
    public static PlayerActionTrigger youClash() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.CLASHES, PlayerRelation.YOU);
    }

    /** "Whenever a player loses the game" */
    public static PlayerActionTrigger playerLosesGame() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.LOSES_GAME, PlayerRelation.ANY);
    }

    /** "Whenever you commit a crime" */
    public static PlayerActionTrigger youCommitCrime() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.COMMITS_CRIME, PlayerRelation.YOU);
    }

    /** "Whenever an opponent commits a crime" */
    public static PlayerActionTrigger opponentCommitsCrime() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.COMMITS_CRIME, PlayerRelation.OPPONENT);
    }

    /** "Whenever a creature you control fights" */
    public static ObjectActionTrigger creatureYouControlFights() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.FIGHTS, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a creature you control explores" */
    public static ObjectActionTrigger creatureYouControlExplores() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.EXPLORES, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a creature you control connives" */
    public static ObjectActionTrigger creatureYouControlConnives() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.CONNIVES, ObjectRelation.YOU_CONTROL);
    }

    /** "When this creature exploits a creature" (CR 702.110b) */
    public static ObjectActionTrigger exploits() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.EXPLOITS, ObjectRelation.THIS);
    }

    /** "Whenever a creature you control exploits a creature" */
    public static ObjectActionTrigger creatureYouControlExploits() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.EXPLOITS, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever a creature you control exploits a nontoken creature" */
    public static ObjectActionTrigger creatureYouControlExploitsNontokenCreature() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.EXPLOITS_NONTOKEN_CREATURE,
                ObjectRelation.YOU_CONTROL);
    }

    /** "When a creature is championed with this creature" */
    public static ObjectActionTrigger championsCreature() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.CHAMPIONS, ObjectRelation.THIS);
    }

    /** "When this card becomes plotted"; it functions from exile, where a plotted card is */
    public static ObjectActionTrigger becomesPlotted() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.BECOMES_PLOTTED, ObjectRelation.THIS);
    }

    /** "Whenever this creature mutates" */
    public static ObjectActionTrigger mutates() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.MUTATES, ObjectRelation.THIS);
    }

    /** "Whenever a creature you control mutates", the mutating creature included */
    public static ObjectActionTrigger creatureYouControlMutates() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.MUTATES, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever this creature crews a Vehicle" */
    public static ObjectActionTrigger crewsVehicle() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.CREWS_VEHICLE, ObjectRelation.THIS);
    }

    /** "Whenever a creature you control crews a Vehicle", this creature included */
    public static ObjectActionTrigger creatureYouControlCrewsVehicle() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.CREWS_VEHICLE, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever this creature saddles a Mount or crews a Vehicle during your main phase" */
    public static ObjectActionTrigger saddlesOrCrewsDuringMainPhase() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.SADDLES_OR_CREWS_DURING_MAIN_PHASE,
                ObjectRelation.THIS);
    }

    /** "Whenever this Vehicle becomes crewed" (CR 702.122e) */
    public static ObjectActionTrigger becomesCrewed() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.BECOMES_CREWED, ObjectRelation.THIS);
    }

    /** "Whenever this Vehicle becomes crewed for the first time each turn" */
    public static ObjectActionTrigger becomesCrewedForFirstTimeEachTurn() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.BECOMES_CREWED_FOR_FIRST_TIME_EACH_TURN,
                ObjectRelation.THIS);
    }

    /** "Whenever this creature becomes saddled" */
    public static ObjectActionTrigger becomesSaddled() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.BECOMES_SADDLED, ObjectRelation.THIS);
    }

    /** "Whenever this creature becomes saddled for the first time each turn" */
    public static ObjectActionTrigger becomesSaddledForFirstTimeEachTurn() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.BECOMES_SADDLED_FOR_FIRST_TIME_EACH_TURN,
                ObjectRelation.THIS);
    }

    /** "Whenever a creature stations this Spacecraft" */
    public static ObjectActionTrigger becomesStationed() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.BECOMES_STATIONED, ObjectRelation.THIS);
    }

    /** "When you unlock [a door of this Room]" (CR 709.5h), any door: the trigger the door collector watches */
    public static ObjectActionTrigger doorUnlocked() {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.DOOR_UNLOCKED, ObjectRelation.THIS);
    }

    /** "When you unlock this door" (CR 709.5h), on the Room's left ({@code 0}) or right ({@code 1}) door */
    public static ObjectActionTrigger doorUnlocked(int door) {
        return new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.DOOR_UNLOCKED, ObjectRelation.THIS, null,
                door);
    }

    /** "Whenever you fully unlock a Room" (CR 709.5i) */
    public static PlayerActionTrigger youFullyUnlockRoom() {
        return new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.FULLY_UNLOCKS_ROOM, PlayerRelation.YOU);
    }

    /** "When you gain control of this from another player" */
    public static ControlChangeTrigger youGainControlOfThis() {
        return new ControlChangeTrigger(ControlChangeTrigger.ControlEvent.YOU_GAIN, ObjectRelation.THIS);
    }

    /** "When you lose control of this" */
    public static ControlChangeTrigger youLoseControlOfThis() {
        return new ControlChangeTrigger(ControlChangeTrigger.ControlEvent.YOU_LOSE, ObjectRelation.THIS);
    }

    /** "Whenever an opponent gains control of a permanent you control" */
    public static ControlChangeTrigger opponentGainsControlOfPermanentYouControl() {
        return new ControlChangeTrigger(ControlChangeTrigger.ControlEvent.OPPONENT_GAINS, ObjectRelation.YOU_CONTROL);
    }

    /** "Whenever an Aura becomes attached to this permanent" */
    public static AttachTrigger auraBecomesAttachedToThis() {
        return new AttachTrigger(AttachTrigger.AttachEvent.AURA_TO_THIS);
    }

    /** "Whenever an Aura you control becomes attached to a creature you control" */
    public static AttachTrigger auraYouControlBecomesAttachedToCreatureYouControl() {
        return new AttachTrigger(AttachTrigger.AttachEvent.AURA_YOU_CONTROL_TO_CREATURE_YOU_CONTROL);
    }

    /**
     * "Whenever an Aura you control becomes attached to a nonland permanent an opponent controls with mana value less
     * than or equal to that Aura's mana value"
     */
    public static AttachTrigger auraYouControlBecomesAttachedToNonlandPermanentOpponentControls() {
        return new AttachTrigger(AttachTrigger.AttachEvent.AURA_YOU_CONTROL_TO_OPPONENT_NONLAND_PERMANENT);
    }

    /** "Whenever this Equipment becomes attached to a creature" */
    public static AttachTrigger becomesAttachedToCreature() {
        return new AttachTrigger(AttachTrigger.AttachEvent.THIS_EQUIPMENT_TO_CREATURE);
    }

    /** "Whenever day becomes night or night becomes day" (CR 731.1a) */
    public static DayNightTrigger dayBecomesNightOrNightBecomesDay() {
        return new DayNightTrigger();
    }

    /** "When you planeswalk to this plane"; it functions from the command zone (CR 901.7) */
    public static PlanarTrigger planeswalkToThis() {
        return new PlanarTrigger(PlanarTrigger.PlanarEvent.PLANESWALKED_TO);
    }

    /** "When you planeswalk away from this plane" */
    public static PlanarTrigger planeswalkAwayFromThis() {
        return new PlanarTrigger(PlanarTrigger.PlanarEvent.PLANESWALKED_FROM);
    }

    /** "Whenever chaos ensues", a plane's chaos ability (CR 311.7) */
    public static PlanarTrigger chaosEnsues() {
        return new PlanarTrigger(PlanarTrigger.PlanarEvent.CHAOS_ENSUES);
    }

    /** "When you encounter this phenomenon" (CR 312.5) */
    public static PlanarTrigger encounterThis() {
        return new PlanarTrigger(PlanarTrigger.PlanarEvent.ENCOUNTERED);
    }

    /**
     * The trigger the state-trigger reader watches (CR 603.8), with no game state. A card's state trigger names its
     * game state: {@link #state(StateTriggerPredicate)}, {@link #sourceState}, {@link #battlefieldHasAtMost},
     * {@link #battlefieldHasAtLeast}.
     */
    public static StateTrigger state() {
        return new StateTrigger();
    }

    /** A state trigger (CR 603.8) on a condition over the whole game: "when you have 30 or more life" */
    public static StateTrigger state(StateTriggerPredicate predicate) {
        return new StateTrigger(Objects.requireNonNull(predicate, "predicate"), null, null, 0, 0, null);
    }

    /**
     * A state trigger (CR 603.8) on the source permanent, checked through the layer-aware predicate evaluator: "when
     * this creature has flying"
     */
    public static StateTrigger sourceState(PermanentPredicate sourcePredicate) {
        return new StateTrigger(null, Objects.requireNonNull(sourcePredicate, "sourcePredicate"), null, 0, 0, null);
    }

    /**
     * A state trigger (CR 603.8) on at most {@code maximumMatches} permanents on the battlefield matching
     * {@code predicate}: "when you control no Islands" is {@code battlefieldHasAtMost(0, islandYouControl)}
     */
    public static StateTrigger battlefieldHasAtMost(int maximumMatches, PermanentPredicate predicate) {
        return new StateTrigger(null, null, Objects.requireNonNull(predicate, "predicate"), maximumMatches, 0, null);
    }

    /**
     * A state trigger (CR 603.8) on at least {@code minimumMatches} permanents on the battlefield matching
     * {@code predicate}
     */
    public static StateTrigger battlefieldHasAtLeast(int minimumMatches, PermanentPredicate predicate) {
        return new StateTrigger(null, null, Objects.requireNonNull(predicate, "predicate"), Integer.MAX_VALUE,
                minimumMatches, null);
    }
}
