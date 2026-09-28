package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEachPlayerAndCreateTokensForMilledCreaturesEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves Dread Summons' mill and token creation. */
@Component
@RequiredArgsConstructor
public class MillEachPlayerAndCreateTokensForMilledCreaturesEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final PermanentControlSupport permanentControlSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillEachPlayerAndCreateTokensForMilledCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillEachPlayerAndCreateTokensForMilledCreaturesEffect) effect;
        int count = Math.max(0, amountEvaluationService.evaluate(
                gameData, e.count(), AmountContext.forStackEntry(entry, null)));

        int creatureCount = 0;
        for (var playerId : gameData.orderedPlayerIds) {
            List<Card> milled = graveyardService.resolveMillPlayer(gameData, playerId, count);
            creatureCount += (int) milled.stream()
                    .filter(card -> card.hasType(CardType.CREATURE))
                    .count();
        }

        entry.setEventValue(creatureCount);
        if (creatureCount > 0) {
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData,
                    entry.getControllerId(),
                    e.tokenTemplate().withAmount(creatureCount),
                    entry.getCard().getSetCode()));
        }
    }
}
