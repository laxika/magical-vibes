package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForExiledCardsWithSourceEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokensForExiledCardsWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensForExiledCardsWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var tokenEffect = (CreateTokensForExiledCardsWithSourceEffect) effect;
        UUID sourcePermanentId = tokenEffect.sourcePermanentId() != null
                ? tokenEffect.sourcePermanentId()
                : entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        Map<UUID, Integer> manaValuesByOwner = new LinkedHashMap<>();
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (sourcePermanentId.equals(exiled.sourcePermanentId())
                        && exiled.ownerId() != null
                        && gameData.playerIds.contains(exiled.ownerId())) {
                    manaValuesByOwner.merge(exiled.ownerId(), exiled.card().getManaValue(), Integer::sum);
                }
            }
        }

        if (manaValuesByOwner.isEmpty()) {
            return;
        }

        CreateTokenEffect blueprint = tokenEffect.tokenEffect();
        AmountContext context = AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot());
        int amount = amountEvaluationService.evaluate(gameData, blueprint.amount(), context);
        if (amount <= 0) {
            return;
        }

        for (Map.Entry<UUID, Integer> ownerEntry : manaValuesByOwner.entrySet()) {
            CreateTokenEffect resolved = blueprint.withPowerToughness(ownerEntry.getValue(), ownerEntry.getValue());
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData, ownerEntry.getKey(), resolved, amount,
                    entry.getCard().getSetCode(), ownerEntry.getValue(), ownerEntry.getValue()));
        }
    }
}
