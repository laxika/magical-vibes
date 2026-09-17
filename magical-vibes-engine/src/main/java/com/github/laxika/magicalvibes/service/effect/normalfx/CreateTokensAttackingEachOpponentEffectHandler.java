package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAttackingEachOpponentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateTokensAttackingEachOpponentEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensAttackingEachOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokensAttackingEachOpponentEffect create = (CreateTokensAttackingEachOpponentEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext context = AmountContext.forStackEntry(entry, source);
        CreateTokenEffect token = create.token();
        int amount = amountEvaluationService.evaluate(gameData, token.amount(), context);
        if (amount <= 0) {
            return;
        }
        int power = amountEvaluationService.evaluate(gameData, token.power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, token.toughness(), context);
        CreateTokenEffect evaluatedToken = token.withAmount(amount).withPowerToughness(power, toughness);

        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (!gameData.playerIds.contains(opponentId) || opponentId.equals(entry.getControllerId())) {
                continue;
            }
            var createdIds = permanentControlSupport.applyCreateToken(
                    gameData, entry.getControllerId(), evaluatedToken,
                    entry.getCard() == null ? null : entry.getCard().getSetCode());
            entry.getCreatedPermanentIds().addAll(createdIds);
            for (UUID createdId : createdIds) {
                Permanent created = gameQueryService.findPermanentById(gameData, createdId);
                if (created != null && created.getCard().hasType(CardType.CREATURE) && created.isAttacking()) {
                    created.setAttackTarget(opponentId);
                }
            }
        }
    }
}
