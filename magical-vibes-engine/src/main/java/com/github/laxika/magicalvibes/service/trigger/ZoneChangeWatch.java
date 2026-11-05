package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.PermanentChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Which of the old zone-change slot collectors puts a triggered ability on the stack, now that the trigger pipeline
 * decides whether it triggers (effect-slot-refactor.md step 12a). The family includes a permanent phasing,
 * transforming and being turned face up, which aren't zone changes (CR 702.26d) but moved with it. Each constant is named after the slot whose
 * collector it stands for and lists the trigger conditions the cards of that slot were converted to: their trigger,
 * the zone the ability functions from and its multiplicity. Its {@link CollectsWatchTrigger} handlers bind the
 * event data the effects need and choose how the ability reaches the stack, as the slot's collector did, until steps
 * 14 and 15 of the refactor replace them with trigger options and effects that read the event.
 */
public enum ZoneChangeWatch {
    /** "Whenever you sacrifice a permanent", "another permanent", "this or another". */
    ON_ALLY_PERMANENT_SACRIFICED(
            battlefield(Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL)),
            battlefield(Triggers.sacrificed(null, ObjectRelation.ANOTHER_YOU_CONTROL)),
            battlefield(Triggers.sacrificed(null, ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL)),
            battlefield(Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL), TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever an opponent sacrifices a permanent". */
    ON_OPPONENT_PERMANENT_SACRIFICED(battlefield(Triggers.sacrificed(null, ObjectRelation.OPPONENT_CONTROLS))),
    /** "Whenever a player sacrifices a permanent", "another permanent". */
    ON_ANY_PERMANENT_SACRIFICED(
            battlefield(Triggers.sacrificed(null, ObjectRelation.ANY)),
            battlefield(Triggers.sacrificed(null, ObjectRelation.ANOTHER))),
    /** "Whenever a noncreature artifact is sacrificed or destroyed". */
    ON_ANY_NONCREATURE_ARTIFACT_SACRIFICED_OR_DESTROYED(battlefield(Triggers.sacrificedOrDestroyed(Triggers.NONCREATURE_ARTIFACT, ObjectRelation.ANY))),
    /** "Whenever an opponent sacrifices a nontoken permanent". */
    ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED(
            battlefield(Triggers.sacrificed(Triggers.NONTOKEN, ObjectRelation.OPPONENT_CONTROLS))),
    /** "Whenever a player sacrifices a creature", "another creature". */
    ON_ANY_CREATURE_SACRIFICED(
            battlefield(Triggers.sacrificed(Triggers.CREATURE, ObjectRelation.ANY)),
            battlefield(Triggers.sacrificed(Triggers.CREATURE, ObjectRelation.ANOTHER))),
    /** "Whenever you sacrifice a token", from exile. */
    EXILE_ON_CONTROLLER_TOKEN_SACRIFICED(
            in(Zone.EXILE, Triggers.sacrificed(Triggers.TOKEN, ObjectRelation.YOU_CONTROL))),
    /** "When you sacrifice a permanent", from the graveyard. */
    GRAVEYARD_ON_CONTROLLER_PERMANENT_SACRIFICED(
            in(Zone.GRAVEYARD, Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL))),
    /** "Whenever a creature is put into your graveyard from the battlefield". */
    ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD(
            battlefield(Triggers.creatureDies(ObjectRelation.YOU_OWN))),
    /** "When a creature is put into your graveyard from the battlefield", from the graveyard (recover). */
    GRAVEYARD_ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD(
            in(Zone.GRAVEYARD, Triggers.creatureDies(ObjectRelation.YOU_OWN))),
    /** "Whenever a creature is put into an opponent's graveyard from the battlefield", from the graveyard. */
    GRAVEYARD_ON_CREATURE_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD(
            in(Zone.GRAVEYARD, Triggers.creatureDies(ObjectRelation.OPPONENT_OWNS))),
    /** "Whenever an artifact [you control, another artifact you control] is put into a graveyard from the battlefield". */
    ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD(
            battlefield(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY)),
            battlefield(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL)),
            battlefield(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL))),
    /** "Whenever an enchantment is put into a graveyard from the battlefield". */
    ON_ANY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD(
            battlefield(Triggers.dies(Triggers.ENCHANTMENT, ObjectRelation.ANY))),
    /** "Whenever an enchantment you control is put into a graveyard from the battlefield". */
    ON_ALLY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD(
            battlefield(Triggers.dies(Triggers.ENCHANTMENT, ObjectRelation.YOU_CONTROL))),
    /** "Whenever an artifact an opponent controls is put into a graveyard from the battlefield". */
    ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD(
            battlefield(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.OPPONENT_CONTROLS))),
    /** "Whenever an artifact is put into an opponent's graveyard from the battlefield". */
    ON_ARTIFACT_PUT_INTO_OPPONENT_OWNER_GRAVEYARD_FROM_BATTLEFIELD(
            battlefield(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.OPPONENT_OWNS))),
    /** "Whenever a permanent an opponent controls is put into a graveyard from the battlefield". */
    ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD(
            battlefield(Triggers.dies(null, ObjectRelation.OPPONENT_CONTROLS))),
    /**
     * "Whenever a permanent is put into a graveyard from the battlefield", where the collector leaves the source's own
     * move out, and "a permanent you control", where it doesn't.
     */
    ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD(battlefield(Triggers.dies(null, ObjectRelation.ANY)),
            battlefield(Triggers.dies(null, ObjectRelation.YOU_CONTROL))),
    /** "Whenever a nontoken permanent is put into a graveyard from the battlefield". */
    ON_ANY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD(
            battlefield(Triggers.dies(Triggers.NONTOKEN, ObjectRelation.ANY))),
    /** "Whenever a nontoken permanent is put into your graveyard from the battlefield". */
    ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD(
            battlefield(Triggers.dies(Triggers.NONTOKEN, ObjectRelation.YOU_OWN))),
    /** "Whenever another permanent you control is put into a graveyard from the battlefield". */
    ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD(
            battlefield(Triggers.dies(null, ObjectRelation.ANOTHER_YOU_CONTROL))),
    /** "Whenever a permanent is put into your graveyard from the battlefield". */
    ON_PERMANENT_PUT_INTO_YOUR_GRAVEYARD_FROM_BATTLEFIELD(battlefield(Triggers.dies(null, ObjectRelation.YOU_OWN))),
    /**
     * "Whenever a permanent is put into an opponent's graveyard from the battlefield", and "a permanent owned by
     * another player", which is the same (CR 400.3).
     */
    ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD(
            battlefield(Triggers.dies(null, ObjectRelation.OPPONENT_OWNS))),
    /** "Whenever a spell or ability an opponent controls causes a land to be put into your graveyard …". */
    ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT(
            battlefield(Triggers.dies(Triggers.LAND, ObjectRelation.YOU_OWN).causedBy(PlayerRelation.OPPONENT))),
    /** "Whenever a spell or ability an opponent controls destroys a noncreature permanent you control". */
    ON_ALLY_NONCREATURE_PERMANENT_DESTROYED_BY_OPPONENT(battlefield(
            Triggers.destroyed(Triggers.NONCREATURE, ObjectRelation.YOU_CONTROL).causedBy(PlayerRelation.OPPONENT))),
    /** "Whenever a land is put into a graveyard from the battlefield". */
    ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD(battlefield(Triggers.dies(Triggers.LAND, ObjectRelation.ANY))),
    /** "When enchanted permanent is put into a graveyard", "when enchanted creature dies". */
    ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD(battlefield(Triggers.dies(ObjectRelation.ENCHANTED))),
    /** "Whenever an Aura or Equipment you control is put into a graveyard from the battlefield". */
    ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD(
            battlefield(Triggers.dies(Triggers.AURA_OR_EQUIPMENT, ObjectRelation.YOU_CONTROL))),
    /** "When enchanted permanent leaves the battlefield". */
    ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD(
            battlefield(Triggers.leavesBattlefield(null, ObjectRelation.ENCHANTED))),
    /** "Whenever another permanent leaves the battlefield". */
    ON_ANOTHER_PERMANENT_LEAVES_BATTLEFIELD(battlefield(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER))),
    /** "Whenever another creature leaves the battlefield". */
    ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD(
            battlefield(Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER))),
    /** "Whenever another artifact you control leaves the battlefield". */
    ON_ANOTHER_ARTIFACT_LEAVES_BATTLEFIELD(
            battlefield(Triggers.leavesBattlefield(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL))),
    /** "Whenever another nontoken artifact you control is put into a graveyard or exiled from the battlefield". */
    ON_ANOTHER_NONTOKEN_ARTIFACT_PUT_INTO_GRAVEYARD_OR_EXILE_FROM_BATTLEFIELD(battlefield(
            Triggers.putIntoGraveyardOrExileFromBattlefield(Triggers.NONTOKEN_ARTIFACT,
                    ObjectRelation.ANOTHER_YOU_CONTROL))),
    /** "Whenever another creature you control leaves the battlefield". */
    ON_ALLY_CREATURE_LEAVES_BATTLEFIELD(
            battlefield(Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL))),
    /** "Whenever another creature you control is put into exile from the battlefield". */
    ON_ALLY_CREATURE_EXILED_FROM_BATTLEFIELD(battlefield(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD),
            Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL))),
    /**
     * "Whenever a creature is put into exile from the battlefield". The collector judges whether the creature's
     * own exile counts, by its effects.
     */
    ON_ANY_CREATURE_EXILED_FROM_BATTLEFIELD(battlefield(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD),
            Triggers.CREATURE, ObjectRelation.ANY))),
    /** "Whenever another artifact is put into exile from the battlefield". */
    ON_ANY_ARTIFACT_EXILED_FROM_BATTLEFIELD(battlefield(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD),
            Triggers.ARTIFACT, ObjectRelation.ANOTHER))),
    /** "Whenever one or more other creatures you control leave the battlefield without dying". */
    ON_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING(battlefield(
            Triggers.leavesBattlefieldWithoutDying(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
            TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever another creature you control leaves the battlefield without dying". */
    ON_ALLY_CREATURE_LEAVES_BATTLEFIELD_WITHOUT_DYING(battlefield(
            Triggers.leavesBattlefieldWithoutDying(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL))),
    /** "Whenever this creature or another creature you control leaves the battlefield without dying". */
    ON_SELF_OR_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING(battlefield(
            Triggers.leavesBattlefieldWithoutDying(Triggers.CREATURE, ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL))),
    /** "Whenever another permanent (artifact) you control leaves the battlefield during your turn". */
    ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD_DURING_CONTROLLER_TURN(
            battlefield(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER_YOU_CONTROL).onlyDuringYourTurn()),
            battlefield(Triggers.leavesBattlefield(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL)
                    .onlyDuringYourTurn())),
    /** "Whenever another permanent you control leaves the battlefield". */
    ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD(
            battlefield(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER_YOU_CONTROL))),
    /** "Whenever one or more permanents you control leave the battlefield". */
    ON_ALLY_PERMANENTS_LEAVE_BATTLEFIELD(battlefield(Triggers.leavesBattlefield(null, ObjectRelation.YOU_CONTROL),
            TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever a spell or ability you control exiles another permanent". */
    ON_CONTROLLER_SPELL_OR_ABILITY_EXILES_PERMANENT(battlefield(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null,
            ObjectRelation.ANOTHER).causedBy(PlayerRelation.YOU))),
    /** "Whenever a permanent is returned to a player's hand". */
    ON_ANY_PERMANENT_RETURNED_TO_HAND(
            battlefield(Triggers.putIntoHandFrom(Zone.BATTLEFIELD, null, ObjectRelation.ANY))),
    /** "Whenever a creature is returned to your hand from the battlefield". */
    ON_CONTROLLER_CREATURE_RETURNED_TO_HAND(
            battlefield(Triggers.putIntoHandFrom(Zone.BATTLEFIELD, Triggers.CREATURE, ObjectRelation.YOU_OWN))),
    /** "Whenever a permanent is returned to your hand". */
    ON_CONTROLLER_PERMANENT_RETURNED_TO_HAND(
            battlefield(Triggers.putIntoHandFrom(Zone.BATTLEFIELD, null, ObjectRelation.YOU_OWN))),
    /** "Whenever another nonland permanent you control is returned to its owner's hand". */
    ON_CONTROLLER_ANOTHER_NONLAND_PERMANENT_RETURNED_TO_HAND(battlefield(
            Triggers.putIntoHandFrom(Zone.BATTLEFIELD, Triggers.NONLAND, ObjectRelation.ANOTHER_YOU_CONTROL))),
    /** "Whenever one or more other cards are put into exile from anywhere". */
    ON_ANY_CARD_EXILED(battlefield(Triggers.exiledFrom(Set.of(), Triggers.NONTOKEN, ObjectRelation.ANOTHER),
            TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever one or more cards are put into exile during your turn". */
    ON_CONTROLLER_CARDS_EXILED_DURING_TURN(battlefield(
            Triggers.exiledFrom(Set.of(), Triggers.NONTOKEN, ObjectRelation.ANY).onlyDuringYourTurn(),
            TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever your commander is put into the command zone from anywhere". */
    ON_YOUR_COMMANDER_PUT_INTO_COMMAND_ZONE(battlefield(Triggers.putIntoCommandZone(null, ObjectRelation.YOU_OWN))),
    /** "Whenever one or more cards are put into exile from your hand". */
    ON_CONTROLLER_CARDS_EXILED_FROM_HAND(battlefield(
            Triggers.exiledFrom(Set.of(Zone.HAND), null, ObjectRelation.YOU_OWN), TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever a card is put into your hand from your library". */
    ON_CONTROLLER_CARD_PUT_INTO_HAND_FROM_LIBRARY(
            battlefield(Triggers.putIntoHandFrom(Zone.LIBRARY, null, ObjectRelation.YOU_OWN))),
    /** "Whenever a land card is put into your graveyard from anywhere"; "one or more land cards". */
    ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE(
            battlefield(Triggers.putIntoGraveyardFromAnywhere(Triggers.LAND_CARD, ObjectRelation.YOU_OWN)),
            battlefield(Triggers.putIntoGraveyardFromAnywhere(Triggers.LAND_CARD, ObjectRelation.YOU_OWN),
                    TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever a land card is put into your graveyard from your library". */
    ON_ALLY_LAND_CARD_MILLED(battlefield(
            Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.LAND_CARD, ObjectRelation.YOU_OWN))),
    /** "Whenever a permanent card is put into your graveyard from your library". */
    ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY(battlefield(
            Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.PERMANENT_CARD, ObjectRelation.YOU_OWN))),
    /** "Whenever one or more creature cards are put into your graveyard from your library". */
    ON_ALLY_CREATURE_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY(battlefield(
            Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_OWN),
            TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever one or more cards are put into your graveyard from your library". */
    ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY(battlefield(
            Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN),
            TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever a creature card is put into a graveyard from a library". */
    ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY(battlefield(
            Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE, ObjectRelation.ANY))),
    /** "Whenever one or more cards are put into a library from anywhere". */
    ON_ANY_CARDS_PUT_INTO_LIBRARY(battlefield(Triggers.putIntoLibrary(Triggers.NONTOKEN, ObjectRelation.ANOTHER),
            TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever another card is put into your graveyard from anywhere". */
    ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE(battlefield(
            Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.ANOTHER_YOU_OWN))),
    /** "Whenever an artifact card is put into your graveyard from anywhere other than the battlefield". */
    ON_ALLY_ARTIFACT_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD(battlefield(
            Triggers.putIntoGraveyardFromAnywhereButBattlefield(Triggers.NONTOKEN_ARTIFACT, ObjectRelation.YOU_OWN))),
    /** "Whenever a nonblack card is put into a graveyard from anywhere". */
    ON_NONBLACK_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE(battlefield(
            Triggers.putIntoGraveyardFromAnywhere(Triggers.NONBLACK_CARD, ObjectRelation.ANOTHER))),
    /**
     * "Whenever a creature card is put into your graveyard from anywhere"; "one or more"; and "from anywhere but your
     * library", where another ability counts the library's cards together (Voracious Brood).
     */
    ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE(
            battlefield(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE,
                    ObjectRelation.ANOTHER_YOU_OWN)),
            battlefield(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE,
                    ObjectRelation.ANOTHER_YOU_OWN), TriggerMultiplicity.ONE_OR_MORE),
            battlefield(Triggers.putIntoGraveyardFromAnywhereBut(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE,
                    ObjectRelation.ANOTHER_YOU_OWN))),
    /** "Whenever a creature card is put into a graveyard from anywhere other than the battlefield". */
    ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD(battlefield(
            Triggers.putIntoGraveyardFromAnywhereButBattlefield(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANY))),
    /** "Whenever another permanent card is put into your graveyard from anywhere"; "one or more". */
    ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE(
            battlefield(Triggers.putIntoGraveyardFromAnywhere(Triggers.PERMANENT_CARD, ObjectRelation.ANOTHER_YOU_OWN)),
            battlefield(Triggers.putIntoGraveyardFromAnywhere(Triggers.PERMANENT_CARD, ObjectRelation.ANOTHER_YOU_OWN),
                    TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever a creature card is put into an opponent's graveyard from anywhere". */
    ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE(battlefield(
            Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.OPPONENT_OWNS))),
    /** "Whenever a card is put into an opponent's graveyard from anywhere", also from exile while suspended. */
    ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE(
            battlefield(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.OPPONENT_OWNS)),
            in(Zone.EXILE, Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.OPPONENT_OWNS))),
    /** "Whenever a black card is put into an opponent's graveyard from anywhere". */
    ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE(battlefield(
            Triggers.putIntoGraveyardFromAnywhere(Triggers.BLACK_CARD, ObjectRelation.OPPONENT_OWNS))),
    /** "Whenever a land card is put into a graveyard from anywhere", from the graveyard. */
    GRAVEYARD_ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE(in(Zone.GRAVEYARD,
            Triggers.putIntoGraveyardFromAnywhere(Triggers.LAND_CARD, ObjectRelation.ANY))),
    /** "Whenever an opponent mills one or more cards", counting the ones a replacement effect sent elsewhere. */
    ON_OPPONENT_MILLS(battlefield(Triggers.mills(null, ObjectRelation.OPPONENT_OWNS), TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever one or more nonland cards are milled". */
    ON_NONLAND_CARDS_MILLED(battlefield(Triggers.mills(Triggers.NONLAND, ObjectRelation.ANY),
            TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever an opponent mills a nonland card", from the graveyard. */
    GRAVEYARD_ON_OPPONENT_NONLAND_CARD_MILLED(in(Zone.GRAVEYARD,
            Triggers.milled(Triggers.NONLAND, ObjectRelation.OPPONENT_OWNS))),
    /** "Whenever a creature card is put into an opponent's graveyard from their library". */
    ON_OPPONENT_CREATURE_CARD_MILLED(battlefield(Triggers.putIntoGraveyardFrom(Zone.LIBRARY,
            Triggers.NONTOKEN_CREATURE, ObjectRelation.OPPONENT_OWNS))),
    /** "Whenever a creature card leaves an opponent's graveyard", from the graveyard. */
    GRAVEYARD_ON_CREATURE_CARD_LEAVES_OPPONENT_GRAVEYARD(in(Zone.GRAVEYARD,
            Triggers.leavesGraveyard(Triggers.NONTOKEN_CREATURE, ObjectRelation.OPPONENT_OWNS))),
    /** "When this card is put into your hand from your graveyard". */
    GRAVEYARD_ON_SELF_RETURNED_TO_HAND(in(Zone.GRAVEYARD,
            Triggers.putIntoHandFrom(Zone.GRAVEYARD, null, ObjectRelation.THIS))),
    /** "Whenever one or more cards leave your graveyard". */
    ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD(battlefield(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN),
            TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever a card is put into your hand from your graveyard". */
    ON_CONTROLLER_CARD_RETURNED_FROM_GRAVEYARD_TO_HAND(
            battlefield(Triggers.putIntoHandFrom(Zone.GRAVEYARD, null, ObjectRelation.YOU_OWN))),
    /** "Whenever an instant or sorcery card leaves your graveyard"; the collector checks the card's types. */
    ON_CONTROLLER_INSTANT_OR_SORCERY_CARD_LEAVES_GRAVEYARD(
            battlefield(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN))),
    /** "Whenever one or more cards are put into exile from your graveyard". */
    ON_CONTROLLER_CARDS_EXILED_FROM_GRAVEYARD(battlefield(
            Triggers.exiledFrom(Set.of(Zone.GRAVEYARD), null, ObjectRelation.YOU_OWN), TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever one or more cards are put into exile from your library and/or your graveyard". */
    ON_CONTROLLER_CARDS_EXILED_FROM_LIBRARY_OR_GRAVEYARD(battlefield(
            Triggers.exiledFrom(Set.of(Zone.LIBRARY, Zone.GRAVEYARD), null, ObjectRelation.YOU_OWN),
            TriggerMultiplicity.ONE_OR_MORE)),
    /**
     * "Whenever one or more creatures you control and/or creature cards in your graveyard are put into exile": one
     * ability of two trigger conditions.
     */
    ON_CONTROLLER_CREATURES_OR_CREATURE_CARDS_EXILED(
            battlefield(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                    TriggerMultiplicity.ONE_OR_MORE),
            battlefield(Triggers.exiledFrom(Set.of(Zone.GRAVEYARD), Triggers.NONTOKEN_CREATURE,
                    ObjectRelation.YOU_OWN), TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever one or more cards an opponent owns are put into exile". */
    ON_OPPONENT_OWNED_CARD_EXILED(battlefield(
            Triggers.exiledFrom(Set.of(), Triggers.NONTOKEN, ObjectRelation.OPPONENT_OWNS),
            TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever one or more cards are put into exile from graveyards and/or the battlefield during your turn". */
    ON_CARDS_EXILED_FROM_GRAVEYARDS_OR_BATTLEFIELD_DURING_YOUR_TURN(battlefield(
            Triggers.exiledFrom(Set.of(Zone.GRAVEYARD, Zone.BATTLEFIELD), Triggers.NONTOKEN, ObjectRelation.ANY)
                    .onlyDuringYourTurn(), TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever one or more creature cards leave your graveyard". */
    ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD(battlefield(
            Triggers.leavesGraveyard(Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_OWN),
            TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever a creature card leaves your graveyard". */
    ON_CONTROLLER_CREATURE_CARD_LEAVES_GRAVEYARD(battlefield(
            Triggers.leavesGraveyard(Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_OWN))),
    /** "Whenever one or more artifact and/or creature cards leave your graveyard". */
    ON_CONTROLLER_ARTIFACT_OR_CREATURE_CARDS_LEAVE_GRAVEYARD(battlefield(
            Triggers.leavesGraveyard(Triggers.ARTIFACT_OR_CREATURE, ObjectRelation.YOU_OWN),
            TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever one or more artifact cards leave your graveyard". */
    ON_CONTROLLER_ARTIFACT_CARDS_LEAVE_GRAVEYARD(battlefield(
            Triggers.leavesGraveyard(Triggers.ARTIFACT, ObjectRelation.YOU_OWN), TriggerMultiplicity.ONE_OR_MORE)),
    /** "When this phases out". */
    ON_SELF_PHASES_OUT(battlefield(Triggers.phasesOut(null, ObjectRelation.THIS))),
    /** "Whenever one or more other permanents phase out". */
    ON_ANY_OTHER_PERMANENT_PHASES_OUT(battlefield(Triggers.phasesOut(null, ObjectRelation.ANOTHER),
            TriggerMultiplicity.ONE_OR_MORE)),
    /** "When this phases in". */
    ON_SELF_PHASES_IN(battlefield(Triggers.phasesIn(null, ObjectRelation.THIS))),
    /** "When this transforms into [its back face]". */
    ON_TRANSFORM_TO_BACK_FACE(battlefield(Triggers.transformsToBackFace(ObjectRelation.THIS))),
    /** "When this transforms into [its front face]". */
    ON_TRANSFORM_TO_FRONT_FACE(battlefield(Triggers.transformsToFrontFace(ObjectRelation.THIS))),
    /** "When equipped creature transforms". */
    ON_EQUIPPED_CREATURE_TRANSFORMS(battlefield(Triggers.transforms(null, ObjectRelation.EQUIPPED))),
    /** "Whenever a permanent you control transforms". */
    ON_ALLY_PERMANENT_TRANSFORMS(battlefield(Triggers.transforms(null, ObjectRelation.YOU_CONTROL))),
    /** "When this is turned face up". */
    ON_TURNED_FACE_UP(battlefield(Triggers.turnedFaceUp(null, ObjectRelation.THIS))),
    /** "Whenever this creature or another creature you control is turned face up", "a creature you control". */
    ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP(
            battlefield(Triggers.turnedFaceUp(Triggers.CREATURE, ObjectRelation.YOU_CONTROL))),
    /** "Whenever this permanent or another permanent is turned face up", "a permanent". */
    ON_SELF_OR_ANY_PERMANENT_TURNS_FACE_UP(battlefield(Triggers.turnedFaceUp(null, ObjectRelation.ANY))),
    /** "Whenever this permanent or another permanent you control is turned face up", "a permanent you control". */
    ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP(
            battlefield(Triggers.turnedFaceUp(null, ObjectRelation.YOU_CONTROL))),
    /** "Whenever a permanent you control is turned face up", from the graveyard. */
    GRAVEYARD_ON_ALLY_PERMANENT_TURNS_FACE_UP(
            in(Zone.GRAVEYARD, Triggers.turnedFaceUp(null, ObjectRelation.YOU_CONTROL))),
    /** "Whenever another creature you control enters". */
    ON_ALLY_CREATURE_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.CREATURE,
            ObjectRelation.ANOTHER_YOU_CONTROL))),
    /** "Whenever one or more other creatures you control enter". */
    ON_ALLY_CREATURES_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.CREATURE,
            ObjectRelation.ANOTHER_YOU_CONTROL), TriggerMultiplicity.ONE_OR_MORE)),
    /**
     * "Whenever this creature or another creature you control enters", "a creature you control", and "this creature
     * or another [restricted] creature you control".
     */
    ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD(
            battlefield(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL)),
            battlefield(Triggers.enters(Triggers.CREATURE, ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL))),
    /** "Whenever this creature and/or one or more other [restricted] creatures you control enter". */
    ON_SELF_OR_ALLY_CREATURES_ENTERS_BATTLEFIELD(
            battlefield(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                    TriggerMultiplicity.ONE_OR_MORE),
            battlefield(Triggers.enters(Triggers.CREATURE, ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                    TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever another nontoken creature you control enters". */
    ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.NONTOKEN_CREATURE,
            ObjectRelation.ANOTHER_YOU_CONTROL))),
    /** "Whenever this creature or another nontoken creature you control enters". */
    ON_SELF_OR_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.NONTOKEN_CREATURE,
            ObjectRelation.YOU_CONTROL))),
    /** "Whenever a nontoken creature you control enters", from the command zone. */
    COMMAND_ZONE_ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD(in(Zone.COMMAND,
            Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_CONTROL))),
    /** "Whenever another artifact you control enters". */
    ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.ARTIFACT,
            ObjectRelation.ANOTHER_YOU_CONTROL))),
    /** "Whenever this or another artifact you control enters", "an artifact you control". */
    ON_SELF_OR_ALLY_ARTIFACT_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.ARTIFACT,
            ObjectRelation.YOU_CONTROL))),
    /** "Whenever another nontoken artifact you control enters". */
    ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.NONTOKEN_ARTIFACT,
            ObjectRelation.ANOTHER_YOU_CONTROL))),
    /** "Whenever this or another nontoken artifact you control enters". */
    ON_SELF_OR_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.NONTOKEN_ARTIFACT,
            ObjectRelation.YOU_CONTROL))),
    /**
     * "Whenever one or more tokens you control enter", "whenever you create one or more tokens", and "whenever a
     * token you control enters" for each token.
     */
    ON_ALLY_TOKEN_ENTERS_BATTLEFIELD(
            battlefield(Triggers.enters(Triggers.TOKEN, ObjectRelation.YOU_CONTROL), TriggerMultiplicity.ONE_OR_MORE),
            battlefield(Triggers.enters(Triggers.TOKEN, ObjectRelation.YOU_CONTROL)),
            battlefield(Triggers.tokensCreated(null, ObjectRelation.YOU_CONTROL), TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever one or more tokens an opponent controls enter", and "a token" for each token. */
    ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD(
            battlefield(Triggers.enters(Triggers.TOKEN, ObjectRelation.OPPONENT_CONTROLS),
                    TriggerMultiplicity.ONE_OR_MORE),
            battlefield(Triggers.enters(Triggers.TOKEN, ObjectRelation.OPPONENT_CONTROLS))),
    /** "Whenever another creature enters". */
    ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER))),
    /** "Whenever a creature enters". */
    ON_ANY_CREATURE_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY))),
    /** "Whenever a creature enters under enchanted player's control" (a Curse). */
    ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.CREATURE,
            ObjectRelation.ENCHANTED_PLAYER_CONTROLS))),
    /** "Whenever a land enters under enchanted player's control" (a Curse). */
    ON_ENCHANTED_PLAYER_LAND_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.LAND,
            ObjectRelation.ENCHANTED_PLAYER_CONTROLS))),
    /** "Whenever a permanent you control enters transformed". */
    ON_ALLY_PERMANENT_ENTERS_TRANSFORMED(battlefield(Triggers.enters(Triggers.TRANSFORMED,
            ObjectRelation.YOU_CONTROL))),
    /** "Whenever a land an opponent controls enters". */
    ON_OPPONENT_LAND_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.LAND,
            ObjectRelation.OPPONENT_CONTROLS))),
    /**
     * Landfall: "whenever a land you control enters", and "one or more lands". The collector judges whether a land's
     * own entry counts.
     */
    ON_ALLY_LAND_ENTERS_BATTLEFIELD(
            battlefield(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL)),
            battlefield(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), TriggerMultiplicity.ONE_OR_MORE)),
    /** "Whenever another Equipment you control enters". */
    ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.EQUIPMENT,
            ObjectRelation.ANOTHER_YOU_CONTROL))),
    /** Constellation: "whenever another enchantment you control enters". */
    ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.ENCHANTMENT,
            ObjectRelation.ANOTHER_YOU_CONTROL))),
    /** Constellation: "whenever this or another enchantment you control enters". */
    ON_SELF_OR_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.ENCHANTMENT,
            ObjectRelation.YOU_CONTROL))),
    /** "Whenever a creature an opponent controls enters". */
    ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(Triggers.CREATURE,
            ObjectRelation.OPPONENT_CONTROLS))),
    /** "Whenever a creature enters from your graveyard"; the collector checks the graveyard. */
    ON_CREATURE_ENTERS_FROM_GRAVEYARD(battlefield(Triggers.entersFrom(Zone.GRAVEYARD, Triggers.CREATURE,
            ObjectRelation.ANY))),
    /** "Whenever this or another permanent enters from a graveyard". */
    ON_PERMANENT_ENTERS_FROM_GRAVEYARD(battlefield(Triggers.entersFrom(Zone.GRAVEYARD, null, ObjectRelation.ANY))),
    /** "Whenever a permanent you control enters from exile". */
    ON_PERMANENT_ENTERS_FROM_EXILE(battlefield(Triggers.entersFrom(Zone.EXILE, null, ObjectRelation.YOU_CONTROL))),
    /** "When this enters from a graveyard". */
    ON_SELF_ENTERS_FROM_GRAVEYARD(battlefield(Triggers.entersFrom(Zone.GRAVEYARD, null, ObjectRelation.THIS))),
    /**
     * "Whenever a permanent enters", "one or more permanents", "a permanent you control", and "a nontoken creature",
     * whose controller the effect names (Overburden, Genesis Chamber).
     */
    ON_ANY_PERMANENT_ENTERS_BATTLEFIELD(
            battlefield(Triggers.enters(null, ObjectRelation.ANY)),
            battlefield(Triggers.enters(null, ObjectRelation.ANY), TriggerMultiplicity.ONE_OR_MORE),
            battlefield(Triggers.enters(null, ObjectRelation.YOU_CONTROL)),
            battlefield(Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANY))),
    /** "Whenever another permanent you control enters". */
    ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD(battlefield(Triggers.enters(null, ObjectRelation.ANOTHER_YOU_CONTROL))),
    /** Landfall from the graveyard. */
    GRAVEYARD_ON_ALLY_LAND_ENTERS_BATTLEFIELD(in(Zone.GRAVEYARD,
            Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL))),
    /** "Whenever a creature you control enters", from the graveyard. */
    GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD(in(Zone.GRAVEYARD,
            Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL))),
    /**
     * "Whenever a permanent you control enters", and "whenever your commander enters", from the graveyard; the
     * collector checks who controls it.
     */
    GRAVEYARD_ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD(in(Zone.GRAVEYARD, Triggers.enters(null, ObjectRelation.ANY))),
    /** "Whenever a creature enters", from the graveyard. */
    GRAVEYARD_ON_ANY_CREATURE_ENTERS_BATTLEFIELD(in(Zone.GRAVEYARD,
            Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY))),
    /**
     * "Whenever another creature enters from your graveyard or is cast from your graveyard", from the graveyard; the
     * collector checks the graveyard.
     */
    GRAVEYARD_ON_CREATURE_ENTERS_FROM_GRAVEYARD_OR_CAST_FROM_GRAVEYARD(in(Zone.GRAVEYARD,
            Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER))),
    /** "Whenever an artifact you control enters", from the graveyard. */
    GRAVEYARD_ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD(in(Zone.GRAVEYARD,
            Triggers.enters(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL))),
    /** "Whenever an enchantment you control enters", from the graveyard. */
    GRAVEYARD_ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD(in(Zone.GRAVEYARD,
            Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.YOU_CONTROL)));


    /**
     * One trigger condition a watch accepts.
     *
     * @param trigger      the ability's trigger
     * @param zone         the zone the ability functions from
     * @param multiplicity the ability's multiplicity
     */
    public record Key(Trigger trigger, Zone zone, TriggerMultiplicity multiplicity) {
    }

    private static final Map<Key, ZoneChangeWatch> BY_KEY = new HashMap<>();

    static {
        for (ZoneChangeWatch watch : values()) {
            for (Key key : watch.keys) {
                ZoneChangeWatch previous = BY_KEY.put(key, watch);
                if (previous != null) {
                    throw new IllegalStateException(key + " belongs to " + previous + " and " + watch);
                }
            }
        }
    }

    private final List<Key> keys;

    ZoneChangeWatch(Key... keys) {
        this.keys = List.of(keys);
    }

    /** Whether this watch is one of the "whenever [an object] enters" collectors (CR 603.6a). */
    public boolean entering() {
        return keys.getFirst().trigger() instanceof ZoneChangeTrigger trigger && trigger.entersBattlefield();
    }

    /**
     * The triggered abilities of {@code card} with a trigger condition this watch accepts, for code that reads a card's
     * abilities of a family rather than having them matched: a face-up plane's, an ability a copy keeps.
     */
    public List<TriggeredAbility> abilitiesOf(Card card) {
        return card.getTriggeredAbilities().stream()
                .filter(ability -> ability.conditions().stream().anyMatch(condition -> keys.stream()
                        .anyMatch(key -> key.trigger().equals(condition.trigger())
                                || of(ability, condition.trigger(), key.zone()) == this)))
                .toList();
    }

    /**
     * The effects of {@link #abilitiesOf} as they are written, without their options: for code that only inspects
     * which effects a card's abilities have. A reader that puts them on the stack reads them through
     * {@link ZoneChangeAbilityReader}.
     */
    public List<CardEffect> effectsOf(Card card) {
        return abilitiesOf(card).stream()
                .flatMap(ability -> ability.effects().stream().map(EffectRegistration::effect))
                .toList();
    }

    /** The trigger conditions this watch accepts. */
    public List<Key> keys() {
        return keys;
    }

    /**
     * The watch whose collector puts {@code ability} on the stack when its trigger condition {@code trigger}
     * triggers it from {@code zone}, or {@code null} when no zone-change slot collector of step 12a does. A trigger
     * that narrows a watched one down by its filter ("whenever you sacrifice a Food", "another nontoken creature
     * card you own with mana value 3 or less") belongs to the watch of the trigger it narrows: the filter
     * {@code AllOf(X, …)} to the watch of filter {@code X}, any other filter to the watch of no filter, unless
     * {@link CreatureDeathWatch} collects it.
     */
    public static ZoneChangeWatch of(TriggeredAbility ability, Trigger trigger, Zone zone) {
        return ofNarrowed(ability, trigger, zone);
    }

    private static ZoneChangeWatch ofFiltered(TriggeredAbility ability, Trigger trigger, Zone zone) {
        TriggerMultiplicity multiplicity = ability.options().multiplicity();
        ZoneChangeWatch exact = BY_KEY.get(new Key(trigger, zone, multiplicity));
        if (exact != null) {
            return exact;
        }
        PermanentPredicate filter = filterOf(trigger);
        if (filter == null) {
            return null;
        }
        if (filter instanceof PermanentAllOfPredicate allOf && !allOf.predicates().isEmpty()) {
            ZoneChangeWatch narrowed = BY_KEY.get(new Key(withFilter(trigger, allOf.predicates().getFirst()), zone,
                    multiplicity));
            if (narrowed != null) {
                return narrowed;
            }
        }
        if (trigger instanceof ZoneChangeTrigger move && CreatureDeathWatch.collects(move, multiplicity)) {
            return null;
        }
        return BY_KEY.get(new Key(withFilter(trigger, null), zone, multiplicity));
    }

    /**
     * {@link #of}, also for a trigger that narrows a watched one down to its controller's turn ("whenever one or more
     * cards leave your graveyard during your turn"): the watch of the trigger without that restriction, which the
     * trigger pipeline checks.
     */
    private static ZoneChangeWatch ofNarrowed(TriggeredAbility ability, Trigger trigger, Zone zone) {
        ZoneChangeWatch watch = ofFiltered(ability, trigger, zone);
        if (watch == null && trigger instanceof ZoneChangeTrigger move && move.duringYourTurn()) {
            watch = ofFiltered(ability, move.anyTurn(), zone);
        }
        return watch;
    }

    private static PermanentPredicate filterOf(Trigger trigger) {
        return switch (trigger) {
            case ZoneChangeTrigger move -> move.filter();
            case PermanentChangeTrigger change -> change.filter();
            default -> null;
        };
    }

    private static Trigger withFilter(Trigger trigger, PermanentPredicate filter) {
        return switch (trigger) {
            case ZoneChangeTrigger move -> move.withFilter(filter);
            case PermanentChangeTrigger change -> change.withFilter(filter);
            default -> trigger;
        };
    }

    private static Key battlefield(Trigger trigger) {
        return new Key(trigger, Zone.BATTLEFIELD, TriggerMultiplicity.EACH);
    }

    private static Key battlefield(Trigger trigger, TriggerMultiplicity multiplicity) {
        return new Key(trigger, Zone.BATTLEFIELD, multiplicity);
    }

    private static Key in(Zone zone, Trigger trigger) {
        return new Key(trigger, zone, TriggerMultiplicity.EACH);
    }
}
