package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachOpponentCreatesTokenEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentCreatesTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachOpponentCreatesTokenEffect) effect;

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext base = AmountContext.forStackEntry(entry, source);

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(entry.getControllerId())) {
                continue;
            }
            AmountContext playerContext = new AmountContext(playerId, source, base.targetPermanentId(),
                    base.xValue(), base.eventValue());
            int amount = amountEvaluationService.evaluate(gameData, e.token().amount(), playerContext);
            if (amount <= 0) {
                continue;
            }
            List<UUID> createdIds = permanentControlSupport.applyCreateToken(gameData, playerId, e.token(),
                    amount, entry.getCard().getSetCode());
            entry.getCreatedPermanentIds().addAll(createdIds);
        }
    }
}
