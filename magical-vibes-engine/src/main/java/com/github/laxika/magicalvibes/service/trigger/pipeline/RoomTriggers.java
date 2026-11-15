package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.rules.RoomEvent;
import com.github.laxika.magicalvibes.model.trigger.ObjectActionTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Triggers the Room abilities of a {@link RoomEvent} (CR 603.2), each handed whole to the stack door with the record
 * of the event. The abilities are read from {@link TriggerIndex} as the layers leave them; an ability's door and event
 * condition are judged here, its intervening "if" (CR 603.4) and limit (CR 603.2h) by the door.
 *
 * <ul>
 *   <li>A door unlocked (CR 709.5h): the Room's own "when you unlock this door" of that door.</li>
 *   <li>A Room fully unlocked (CR 709.5i): its controller's permanents' "whenever you fully unlock a Room", the Room
 *       as the triggering permanent, then the cards' in their graveyard (Fear of Infinity, CR 113.6).</li>
 * </ul>
 */
@Service
public class RoomTriggers {

    private static final Trigger DOOR_UNLOCKED = Triggers.doorUnlocked();
    private static final Trigger FULLY_UNLOCKS_ROOM = Triggers.youFullyUnlockRoom();

    private final EventAbilitySupport abilities;

    public RoomTriggers(EventAbilitySupport abilities) {
        this.abilities = abilities;
    }

    /** Triggers the abilities {@code event} triggers. */
    public void trigger(GameData gameData, RoomEvent event) {
        Permanent room = event.room();
        UUID controllerId = event.controllerId();
        if (!room.getCard().hasType(CardType.ENCHANTMENT) || !room.getCard().getSubtypes().contains(CardSubtype.ROOM)) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || battlefield.stream().noneMatch(permanent -> permanent.getId().equals(room.getId()))) {
            return;
        }
        switch (event) {
            case RoomEvent.DoorUnlocked unlocked -> doorUnlocked(gameData, room, controllerId, unlocked.doorIndex());
            case RoomEvent.FullyUnlocked ignored -> fullyUnlocked(gameData, room, controllerId, battlefield);
        }
    }

    private void doorUnlocked(GameData gameData, Permanent room, UUID controllerId, int doorIndex) {
        if (doorIndex < 0 || doorIndex > 1) {
            return;
        }
        for (TriggeredAbility ability : abilities.abilities(gameData, room, DOOR_UNLOCKED)) {
            ObjectActionTrigger condition = EventAbilitySupport.condition(ability, ObjectActionTrigger.class,
                    DOOR_UNLOCKED);
            if (condition.door() == null) {
                throw new IllegalStateException("A \"when you unlock this door\" ability names its door: " + ability);
            }
            if (condition.door() != doorIndex) {
                continue;
            }
            abilities.put(gameData, ability, room.getCard(), room, controllerId,
                    ConditionContext.forPermanent(room, controllerId),
                    EventAbilitySupport.eventRecord(room, null, null, null, null, null));
        }
    }

    private void fullyUnlocked(GameData gameData, Permanent room, UUID controllerId, List<Permanent> battlefield) {
        for (Permanent permanent : List.copyOf(battlefield)) {
            for (TriggeredAbility ability : abilities.abilities(gameData, permanent, FULLY_UNLOCKS_ROOM)) {
                Permanent snapshot = new Permanent(permanent);
                abilities.put(gameData, ability, permanent.getCard(), permanent, controllerId,
                        ConditionContext.forPermanent(permanent, controllerId), entry -> {
                            entry.setNonTargeting(true);
                            entry.setSourcePermanentSnapshot(snapshot);
                            entry.setTriggeringPermanentId(room.getId());
                        });
            }
        }
        for (Card card : List.copyOf(gameData.playerGraveyards.getOrDefault(controllerId, List.of()))) {
            for (TriggeredAbility ability : abilities.graveyardAbilities(gameData, card, FULLY_UNLOCKS_ROOM)) {
                putGraveyardAbility(gameData, ability, card, controllerId);
            }
        }
    }

    /**
     * Hands {@code ability} of {@code card} in its owner {@code ownerId}'s graveyard to the stack door, the record naming
     * this graveyard incarnation of the card as its source (CR 400.7).
     */
    private void putGraveyardAbility(GameData gameData, TriggeredAbility ability, Card card, UUID ownerId) {
        long graveyardEntryVersion = gameData.graveyardEntryVersion(card.getId());
        abilities.put(gameData, ability, card, null, ownerId, ConditionContext.forCard(card, ownerId), entry -> {
            entry.setNonTargeting(true);
            entry.setSourceGraveyardEntryVersion(graveyardEntryVersion);
        });
    }
}
