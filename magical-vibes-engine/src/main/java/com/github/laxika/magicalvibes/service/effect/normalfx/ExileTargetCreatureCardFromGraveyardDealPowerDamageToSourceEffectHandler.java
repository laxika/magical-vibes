package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardDealPowerDamageToSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Selfless Exorcist's graveyard exile and power-damage ability. */
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureCardFromGraveyardDealPowerDamageToSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureCardFromGraveyardDealPowerDamageToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        Card targetCard = targetCardId == null
                ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target no longer in a graveyard)."));
            return;
        }

        int power = targetCard.getPower() == null ? 0 : Math.max(0, targetCard.getPower());
        if (!graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, targetCardId, targetCard)) {
            return;
        }

        gameLogService.append(gameData,
                GameLog.textCardText(gameData.playerIdToName.get(entry.getControllerId()) + " exiles ",
                        targetCard, " from a graveyard."));

        if (power == 0 || entry.getSourcePermanentId() == null) {
            return;
        }

        var recipient = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (recipient == null) {
            return;
        }

        UUID damageControllerId = targetCard.getOwnerId() != null
                ? targetCard.getOwnerId() : entry.getControllerId();
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                targetCard,
                damageControllerId,
                targetCard.getName() + "'s ability",
                List.of(),
                null,
                List.of());
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, damageEntry);
        damageSupport.dealCreatureDamage(gameData, damageEntry, recipient, rawDamage);
    }
}
