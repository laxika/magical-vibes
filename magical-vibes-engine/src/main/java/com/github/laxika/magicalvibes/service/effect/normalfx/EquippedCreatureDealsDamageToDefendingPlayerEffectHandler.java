package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EquippedCreatureDealsDamageToDefendingPlayerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EquippedCreatureDealsDamageToDefendingPlayerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EquippedCreatureDealsDamageToDefendingPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EquippedCreatureDealsDamageToDefendingPlayerEffect) effect;
        Permanent creature = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        Card damageSourceCard = creature != null ? creature.getCard() : entry.getDamageSourceCard();
        UUID attackedTargetId = entry.getAttackedTargetId();
        if (damageSourceCard == null || attackedTargetId == null) {
            return;
        }

        UUID defendingPlayerId = gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
        UUID creatureControllerId = creature != null
                ? gameQueryService.findPermanentController(gameData, creature.getId())
                : entry.getTriggeringPermanentControllerId();
        if (defendingPlayerId == null || creatureControllerId == null) {
            return;
        }

        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                damageSourceCard,
                creatureControllerId,
                damageSourceCard.getName() + "'s ability",
                List.of(),
                defendingPlayerId,
                entry.getTriggeringPermanentId());
        damageEntry.setDamageSourceCard(damageSourceCard);
        int amount = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(damageEntry, creature));
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, amount, damageEntry);
        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, damageEntry)) {
            damageSupport.dealDamageToPlayer(gameData, damageEntry, defendingPlayerId, rawDamage);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
