package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.SpellCastFromOutsideStartingDeckTriggerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;

/** Collects triggers for spells whose physical card identity was not in the caster's starting deck. */
@Service
@RequiredArgsConstructor
public class SpellCastFromOutsideStartingDeckTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = SpellCastFromOutsideStartingDeckTriggerEffect.class,
            slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handle(TriggerMatchContext match,
            SpellCastFromOutsideStartingDeckTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast spellCast = (TriggerContext.SpellCast) ctx;
        Set<java.util.UUID> startingDeckCardIds = match.gameData().startingDeckCardIds
                .getOrDefault(spellCast.castingPlayerId(), Set.of());
        if (startingDeckCardIds.contains(spellCast.spellCard().getId())) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(trigger.resolvedEffects()),
                null,
                match.permanent().getId());
        entry.setTriggeringCardId(spellCast.spellCard().getId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        return true;
    }
}
