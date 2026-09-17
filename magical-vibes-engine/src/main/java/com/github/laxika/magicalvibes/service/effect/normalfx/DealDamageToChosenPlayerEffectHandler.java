package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToChosenPlayerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Saskia's damage against the player chosen as Saskia entered. */
@Component
@RequiredArgsConstructor
public class DealDamageToChosenPlayerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToChosenPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var chosenPlayerEffect = (DealDamageToChosenPlayerEffect) effect;

        Permanent abilitySource = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (abilitySource == null) {
            abilitySource = entry.getSourcePermanentSnapshot();
        }
        UUID chosenPlayerId = abilitySource == null
                ? null
                : abilitySource.getProtectionFromPlayerIdsPermanently().stream().findFirst().orElse(null);
        if (chosenPlayerId == null || !gameData.playerIds.contains(chosenPlayerId)) {
            return;
        }

        Permanent dealer = entry.getTriggeringPermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        Card damageSourceCard = dealer == null ? entry.getDamageSourceCard() : dealer.getCard();
        UUID dealerControllerId = dealer == null
                ? entry.getTriggeringPermanentControllerId()
                : gameQueryService.findPermanentController(gameData, dealer.getId());
        if (damageSourceCard == null || dealerControllerId == null) {
            return;
        }

        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                damageSourceCard,
                dealerControllerId,
                damageSourceCard.getName() + "'s ability",
                List.of(),
                chosenPlayerId,
                dealer == null ? null : dealer.getId());
        damageEntry.setDamageSourceCard(damageSourceCard);
        int amount = amountEvaluationService.evaluate(gameData, chosenPlayerEffect.damage(),
                AmountContext.forStackEntry(entry, dealer));
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, amount, damageEntry);
        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, damageEntry)) {
            damageSupport.dealDamageToPlayer(gameData, damageEntry, chosenPlayerId, rawDamage);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
