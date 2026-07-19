package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToCombatOpponentControllerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DealDamageToCombatOpponentControllerEffect}: the source permanent deals its
 * evaluated {@code amount} to the controller of the combat opponent baked into the trigger's
 * {@code targetId}. The stack entry's source stays this permanent, so {@code SourcePower} reads it
 * and opponent-source damage shields keep applying. Mirrors the {@code TARGET_PERMANENT_CONTROLLER}
 * branch of {@link DealDamageToPlayersEffectHandler}.
 */
@Component
@RequiredArgsConstructor
public class DealDamageToCombatOpponentControllerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToCombatOpponentControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToCombatOpponentControllerEffect) effect;

        Permanent opponent = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (opponent == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, opponent.getId());
        if (controllerId == null) {
            return;
        }

        String cardName = entry.getCard().getName();
        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    cardName + "'s damage to " + gameData.playerIdToName.get(controllerId) + " is prevented."));
        } else {
            Permanent source = entry.getSourcePermanentId() != null
                    ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                    : null;
            if (source == null) {
                source = entry.getSourcePermanentSnapshot();
            }
            int amount = amountEvaluationService.evaluate(gameData, e.amount(), AmountContext.forStackEntry(entry, source));
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, amount, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, controllerId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
