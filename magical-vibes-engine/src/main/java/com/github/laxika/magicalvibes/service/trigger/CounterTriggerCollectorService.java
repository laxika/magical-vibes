package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects triggers caused by a spell being countered by a spell or ability a player controls. */
@Service
public class CounterTriggerCollectorService {

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_CONTROLLER_COUNTERS_SPELL)
    private boolean handleControllerCountersSpell(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.SpellCountered countered = (TriggerContext.SpellCountered) ctx;
        GameData gameData = match.gameData();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                countered.counteringPlayerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        gameData.enqueueTrigger(entry);
        return true;
    }
}
