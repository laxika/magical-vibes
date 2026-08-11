package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.RevealCardsChooseOneToExileEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the dynamic reveal-and-exile choice used by Taster of Wares. */
@Component
@RequiredArgsConstructor
public class RevealCardsChooseOneToExileEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealCardsChooseOneToExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealCardsChooseOneToExileEffect) effect;
        int revealCount = amountEvaluationService.evaluate(
                gameData, e.revealCount(), AmountContext.forStackEntry(entry, null));
        if (revealCount <= 0) {
            return;
        }

        playerInteractionSupport.beginRevealCardsChooseDiscard(
                gameData, entry, revealCount, 1, HandChoiceDestination.EXILE,
                entry.getSourcePermanentId());
    }
}
