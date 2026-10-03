package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CreateTokenCopyOfCardEffect) effect;
        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int copyCount = amountEvaluationService.evaluate(
                gameData, copyEffect.tokenCopyEffect().amount(), AmountContext.forStackEntry(entry, sourcePermanent));
        if (copyCount <= 0) {
            return;
        }
        Card sourceCard = copyEffect.sourceCard();
        if (sourceCard == null && entry.getTriggeringCardId() != null) {
            sourceCard = gameQueryService.findCardById(gameData, entry.getTriggeringCardId());
        }
        if (sourceCard == null) {
            return;
        }
        tokenCopySupport.createTokenCopies(gameData, entry,
                Collections.nCopies(copyCount, sourceCard),
                sourcePermanent, copyEffect.tokenCopyEffect());
    }
}
