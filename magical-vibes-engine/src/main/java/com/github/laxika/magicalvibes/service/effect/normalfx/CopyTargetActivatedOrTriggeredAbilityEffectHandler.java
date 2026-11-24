package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTargetActivatedOrTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link CopyTargetActivatedOrTriggeredAbilityEffect}: the target ability, if it is still on the stack and
 * can be copied, is copied the stated number of times for the resolving ability's controller (CR 707.10), each copy
 * through {@link CopyControllerActivatedAbilityEffectHandler#copy}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CopyTargetActivatedOrTriggeredAbilityEffectHandler implements NormalEffectHandlerBean {

    private final CopyControllerActivatedAbilityEffectHandler abilityCopier;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyTargetActivatedOrTriggeredAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copy = (CopyTargetActivatedOrTriggeredAbilityEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }
        StackEntry target = gameData.stack.stream()
                .filter(onStack -> targetId.equals(onStack.getTargetableId())
                        && StackEntryType.ABILITIES.contains(onStack.getEntryType()))
                .findFirst().orElse(null);
        if (target == null) {
            log.info("Game {} - The ability to copy is no longer on the stack", gameData.id);
            return;
        }
        int copies = amountEvaluationService.evaluate(gameData, copy.copies(), AmountContext.forStackEntry(entry, null));
        for (int i = 0; i < copies; i++) {
            abilityCopier.copy(gameData, entry, target, entry.getControllerId(), null);
        }
    }
}
