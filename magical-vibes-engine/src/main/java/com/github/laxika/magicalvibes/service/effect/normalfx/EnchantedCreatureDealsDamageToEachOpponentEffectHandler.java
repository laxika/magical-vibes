package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageToEachOpponentEffect;
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
public class EnchantedCreatureDealsDamageToEachOpponentEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedCreatureDealsDamageToEachOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EnchantedCreatureDealsDamageToEachOpponentEffect) effect;
        Permanent enchantedCreature = findEnchantedCreature(gameData, entry);
        Card sourceCard = enchantedCreature == null ? entry.getDamageSourceCard() : enchantedCreature.getCard();
        if (sourceCard == null) return;

        UUID sourceControllerId = enchantedCreature == null
                ? entry.getTriggeringPermanentControllerId()
                : gameQueryService.findPermanentController(gameData, enchantedCreature.getId());
        if (sourceControllerId == null) return;

        Permanent amountSource = entry.getSourcePermanentSnapshot();
        int damage = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, amountSource));
        if (damage <= 0) return;

        UUID sourcePermanentId = enchantedCreature == null ? entry.getTriggeringPermanentId() : enchantedCreature.getId();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(entry.getControllerId())) continue;

            StackEntry damageEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    sourceControllerId,
                    sourceCard.getName() + "'s ability",
                    List.of(),
                    playerId,
                    sourcePermanentId);
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, damage, damageEntry);
            damageSupport.dealDamageToPlayer(gameData, damageEntry, playerId, rawDamage);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }

    private Permanent findEnchantedCreature(GameData gameData, StackEntry entry) {
        UUID sourcePermanentId = entry.getTriggeringPermanentId();
        if (sourcePermanentId != null) {
            Permanent current = gameQueryService.findPermanentById(gameData, sourcePermanentId);
            if (current != null) return current;
        }
        return entry.getAttachedPermanentSnapshot();
    }
}
