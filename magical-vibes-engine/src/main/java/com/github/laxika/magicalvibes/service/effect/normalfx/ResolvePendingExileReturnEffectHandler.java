package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ResolvePendingExileReturnEffect;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Delegates return options and attacking choices to the existing return resolver. */
@Component
public class ResolvePendingExileReturnEffectHandler implements NormalEffectHandlerBean {
    private final StepTriggerService stepTriggerService;
    private final PermanentRemovalService permanentRemovalService;

    public ResolvePendingExileReturnEffectHandler(@Lazy StepTriggerService stepTriggerService,
                                                @Lazy PermanentRemovalService permanentRemovalService) {
        this.stepTriggerService = stepTriggerService;
        this.permanentRemovalService = permanentRemovalService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ResolvePendingExileReturnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ResolvePendingExileReturnEffect) effect;
        long version = returnEffect.expectedExileEntryVersion() >= 0
                ? returnEffect.expectedExileEntryVersion() : entry.getTriggeringCardExileEntryVersion();
        var cardId = returnEffect.pending().card().getId();
        if (version >= 0 && version != gameData.exileEntryVersions.getOrDefault(
                cardId, -1L)) return;
        if (returnEffect.pending().returnToHand()) {
            permanentRemovalService.resolvePendingExileReturn(
                    gameData, entry.getSourcePermanentId(), returnEffect.pending());
            return;
        }
        stepTriggerService.resolvePendingExileReturn(gameData, returnEffect.pending());
    }
}
