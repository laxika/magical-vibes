package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LeavingPermanentCountersAwareEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects triggered abilities caused by a commander's movement into the command zone. */
@Service
public class CommanderZoneTriggerCollectorService {

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_YOUR_COMMANDER_PUT_INTO_COMMAND_ZONE)
    private boolean handleCommanderPutIntoCommandZoneMay(TriggerMatchContext match,
                                                         MayEffect may, TriggerContext ctx) {
        TriggerContext.CommanderPutIntoCommandZone commander =
                (TriggerContext.CommanderPutIntoCommandZone) ctx;
        boolean needsCounterSnapshot = may.wrapped() instanceof LeavingPermanentCountersAwareEffect
                || may.elseEffect() instanceof LeavingPermanentCountersAwareEffect;
        CardEffect resolved = may;
        if (needsCounterSnapshot) {
            if (commander.commanderCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0) <= 0) {
                return false;
            }
            resolved = ((LeavingPermanentCountersAwareEffect) may)
                    .boundToLeavingPermanentCounters(commander.commanderCounters());
        }
        return enqueue(match, resolved);
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_YOUR_COMMANDER_PUT_INTO_COMMAND_ZONE)
    private boolean handleCommanderPutIntoCommandZoneDefault(TriggerMatchContext match,
                                                              CardEffect effect, TriggerContext ctx) {
        return enqueue(match, effect);
    }

    private boolean enqueue(TriggerMatchContext match, CardEffect effect) {
        Permanent source = match.permanent();
        if (source == null) {
            return false;
        }

        Card sourceCard = source.getCard();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)),
                    "commander put into the command zone", source.getId(), new Permanent(source), null));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    source.getId());
            entry.setSourcePermanentSnapshot(new Permanent(source));
            match.gameData().enqueueTrigger(entry);
        }
        return true;
    }
}
