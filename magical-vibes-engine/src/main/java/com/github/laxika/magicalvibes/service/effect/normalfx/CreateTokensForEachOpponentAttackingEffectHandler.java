package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachOpponentAttackingEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokensForEachOpponentAttackingEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensForEachOpponentAttackingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokensForEachOpponentAttackingEffect) effect;
        CreateTokenEffect token = e.token();

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int amount = amountEvaluationService.evaluate(gameData, token.amount(), context);
        if (amount <= 0) {
            return;
        }
        int power = amountEvaluationService.evaluate(gameData, token.power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, token.toughness(), context);

        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (!gameData.playerIds.contains(opponentId) || opponentId.equals(entry.getControllerId())) {
                continue;
            }

            List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                    gameData, entry.getControllerId(), token, amount, entry.getCard().getSetCode(), power, toughness);
            entry.getCreatedPermanentIds().addAll(createdIds);

            for (UUID createdId : createdIds) {
                Permanent created = gameQueryService.findPermanentById(gameData, createdId);
                if (created != null && gameQueryService.isCreature(gameData, created)) {
                    created.setMustAttackThisTurn(true);
                    created.setMustAttackTargetId(opponentId);
                }
            }
        }
    }
}
