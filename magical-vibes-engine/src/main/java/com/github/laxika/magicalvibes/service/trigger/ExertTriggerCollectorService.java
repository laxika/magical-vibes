package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExertTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_EXERTS)
    private boolean handleExert(TriggerMatchContext match, TriggerContext context) {
        TriggerContext.Exert exert = (TriggerContext.Exert) context;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        entry.setTargetId(exert.exertedCreatureId());
        entry.setNonTargeting(true);
        match.gameData().handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            // "Whenever you exert a creature, tap target creature …" (Vizier of the True): the
            // target is chosen as the trigger is put on the stack.
            match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    match.permanent().getCard(), match.controllerId(), match.stackEffects(),
                    "exerted a creature", match.permanent().getId(), new Permanent(match.permanent()), null));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            return true;
        }
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }
}
