package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.rules.DayNightEvent;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Triggers the "whenever day becomes night or night becomes day" abilities of a {@link DayNightEvent} (CR 603.2): for
 * each player in turn order, those of their permanents, as the layers leave them ({@link TriggerIndex}), then those of
 * the cards in their graveyard (CR 113.6), each handed whole to the stack door with its source as it was (a graveyard
 * card's stay, CR 400.7). The game first becoming day or night triggers none (CR 732.1a).
 */
@Service
public class DayNightTriggers {

    private static final Trigger DAY_NIGHT_CHANGE = Triggers.dayBecomesNightOrNightBecomesDay();

    private final EventAbilitySupport abilities;

    public DayNightTriggers(EventAbilitySupport abilities) {
        this.abilities = abilities;
    }

    /** Triggers the abilities {@code event} triggers. */
    public void trigger(GameData gameData, DayNightEvent event) {
        if (event.previous() == event.current() || event.previous() == DayNight.NEITHER
                || event.current() == DayNight.NEITHER) {
            return;
        }
        for (UUID controllerId : gameData.orderedPlayerIds) {
            for (Permanent permanent : List.copyOf(gameData.playerBattlefields.getOrDefault(controllerId,
                    List.of()))) {
                for (TriggeredAbility ability : abilities.abilities(gameData, permanent, DAY_NIGHT_CHANGE)) {
                    abilities.put(gameData, ability, permanent.getCard(), permanent, controllerId,
                            ConditionContext.forPermanent(permanent, controllerId),
                            EventAbilitySupport.eventRecord(permanent, null, null, null, null, null));
                }
            }
            for (Card card : List.copyOf(gameData.playerGraveyards.getOrDefault(controllerId, List.of()))) {
                for (TriggeredAbility ability : abilities.graveyardAbilities(gameData, card, DAY_NIGHT_CHANGE)) {
                    putGraveyardAbility(gameData, ability, card, controllerId);
                }
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
