package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayFromOutsideHandTriggerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/** Collects triggers for spells cast and lands played from outside their controller's hand. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayFromOutsideHandTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = PlayFromOutsideHandTriggerEffect.class,
            slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleControllerCastsSpellFromOutsideHand(TriggerMatchContext match,
            PlayFromOutsideHandTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast spellCast = (TriggerContext.SpellCast) ctx;
        if (spellCast.castZone() == Zone.HAND) {
            return false;
        }
        return enqueue(match, trigger);
    }

    @CollectsTrigger(value = PlayFromOutsideHandTriggerEffect.class,
            slot = EffectSlot.ON_CONTROLLER_PLAYS_LAND)
    private boolean handleControllerPlaysLandFromOutsideHand(TriggerMatchContext match,
            PlayFromOutsideHandTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.LandPlayed landPlayed = (TriggerContext.LandPlayed) ctx;
        if (landPlayed.playZone() == Zone.HAND) {
            return false;
        }
        return enqueue(match, trigger);
    }

    private boolean enqueue(TriggerMatchContext match, PlayFromOutsideHandTriggerEffect trigger) {
        Card sourceCard = match.permanent().getCard();
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(trigger.resolvedEffects()),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers for a card played from outside hand",
                match.gameData().id, sourceCard.getName());
        return true;
    }
}
