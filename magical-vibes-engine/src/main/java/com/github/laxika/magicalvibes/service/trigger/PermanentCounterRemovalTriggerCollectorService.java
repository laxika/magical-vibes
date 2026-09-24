package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects triggers caused by counters being removed from a permanent the watcher controls. */
@Service
public class PermanentCounterRemovalTriggerCollectorService {

    @CollectsTrigger(value = SeekEffect.class, slot = EffectSlot.ON_ALLY_COUNTERS_REMOVED_FROM_PERMANENT)
    private boolean handleCountersRemoved(TriggerMatchContext match, SeekEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CountersRemovedFromPermanent removed)
                || removed.amount() <= 0 || match.permanent() == null) {
            return false;
        }

        GameData gameData = match.gameData();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()));
        return true;
    }
}
