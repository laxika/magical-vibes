package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves {@link DealDamageToRandomOpponentEffect}. */
@Component
@RequiredArgsConstructor
public class DealDamageToRandomOpponentEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToRandomOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToRandomOpponentEffect) effect;
        UUID chosenOpponent;
        if (e.targeted()) {
            chosenOpponent = entry.getTargetId();
            if (chosenOpponent == null || !gameData.playerIds.contains(chosenOpponent)) {
                return;
            }
        } else {
            List<UUID> opponents = gameData.orderedPlayerIds.stream()
                    .filter(playerId -> !playerId.equals(entry.getControllerId()))
                    .toList();
            if (opponents.isEmpty()) {
                return;
            }
            chosenOpponent = opponents.get(ThreadLocalRandom.current().nextInt(opponents.size()));
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, damage, entry);
        damageSupport.resolveAnyTargetDamage(gameData, entry, chosenOpponent, rawDamage, false);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
