package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerCreatesTokenEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerCreatesTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerCreatesTokenEffect) effect;

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext base = AmountContext.forStackEntry(entry, source);

        for (UUID playerId : gameData.orderedPlayerIds) {
            // Re-evaluate the token count relative to each creating player so CONTROLLER-scoped
            // counts read that player's own board (Waiting in the Weeds: untapped Forests they control).
            AmountContext playerContext = new AmountContext(playerId, source, base.targetPermanentId(),
                    base.xValue(), base.eventValue(), false);
            int amount = amountEvaluationService.evaluate(gameData, e.token().amount(), playerContext);
            if (amount <= 0) {
                continue;
            }
            entry.getCreatedPermanentIds().addAll(
                    permanentControlSupport.applyCreateToken(gameData, playerId, e.token(), amount,
                            entry.getCard().getSetCode()));
        }
    }
}
