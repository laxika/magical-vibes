package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects triggered abilities caused by a commander's movement into the command zone. A commander that left the
 * battlefield is the stack entry's triggering permanent, as it last existed there (CR 603.10a).
 */
@Service
public class CommanderZoneTriggerCollectorService {

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_YOUR_COMMANDER_PUT_INTO_COMMAND_ZONE)
    private boolean handleCommanderPutIntoCommandZoneDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CommanderPutIntoCommandZone commander = (TriggerContext.CommanderPutIntoCommandZone) ctx;
        return enqueue(match, commander.departedCommander());
    }

    private boolean enqueue(TriggerMatchContext match, Permanent departedCommander) {
        Permanent source = match.permanent();
        if (source == null) {
            return false;
        }

        Card sourceCard = source.getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                source.getId());
        entry.setSourcePermanentSnapshot(new Permanent(source));
        entry.setTriggeringPermanent(departedCommander);
        match.gameData().handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)
                || match.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), match.stackEffects(),
                    "commander put into the command zone", source.getId(), new Permanent(source), null)
                    .withTriggeringPermanent(departedCommander));
        } else {
            match.gameData().enqueueTrigger(entry);
        }
        return true;
    }
}
