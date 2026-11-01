package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Which of the old zone-change slot collectors puts a triggered ability on the stack, now that the trigger pipeline
 * decides whether it triggers (effect-slot-refactor.md step 12a). Each constant is named after the slot whose
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
    /** "Whenever an artifact is put into a graveyard from the battlefield". */
    ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD(
            battlefield(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY))),
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
     * "Whenever a permanent is put into a graveyard from the battlefield". The collector judges whether the
     * permanent's own move counts, by its effects' wrappers.
     */
    ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD(battlefield(Triggers.dies(null, ObjectRelation.ANY))),
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
    /** "Whenever another permanent you control leaves the battlefield during your turn". */
    ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD_DURING_CONTROLLER_TURN(battlefield(
            Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER_YOU_CONTROL).onlyDuringYourTurn())),
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
            Triggers.leavesGraveyard(Triggers.ARTIFACT, ObjectRelation.YOU_OWN), TriggerMultiplicity.ONE_OR_MORE));

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

    /** The trigger conditions this watch accepts. */
    public List<Key> keys() {
        return keys;
    }

    /**
     * The watch whose collector puts {@code ability} on the stack when its trigger condition {@code trigger}
     * triggers it from {@code zone}, or {@code null} when no zone-change slot collector of step 12a does.
     */
    public static ZoneChangeWatch of(TriggeredAbility ability, Trigger trigger, Zone zone) {
        return BY_KEY.get(new Key(trigger, zone, ability.options().multiplicity()));
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
