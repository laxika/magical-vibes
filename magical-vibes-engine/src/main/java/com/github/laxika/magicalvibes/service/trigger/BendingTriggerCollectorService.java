package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BendingTriggerCollectorService {

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_CONTROLLER_BENDS)
    private boolean handleBending(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);
        return true;
    }
}
