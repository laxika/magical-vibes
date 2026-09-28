package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndCreateTokensForMilledCreaturesEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a controller mill followed by one token per creature card milled. */
@Component
@RequiredArgsConstructor
public class MillControllerAndCreateTokensForMilledCreaturesEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final PermanentControlSupport permanentControlSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndCreateTokensForMilledCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillControllerAndCreateTokensForMilledCreaturesEffect) effect;
        int count = Math.max(0, amountEvaluationService.evaluate(
                gameData, e.count(), AmountContext.forStackEntry(entry, null)));

        UUID controllerId = entry.getControllerId();
        List<Card> milled = graveyardService.resolveMillPlayer(gameData, controllerId, count);
        int creatureCount = (int) milled.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .count();

        entry.setEventValue(creatureCount);
        if (creatureCount > 0) {
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData,
                    controllerId,
                    e.tokenTemplate().withAmount(creatureCount),
                    entry.getCard().getSetCode()));
        }
    }
}
