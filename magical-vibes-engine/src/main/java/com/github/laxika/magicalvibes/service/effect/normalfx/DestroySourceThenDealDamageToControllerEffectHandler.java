package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySourceThenDealDamageToControllerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves source self-destruction followed by damage from that source. */
@Component
@RequiredArgsConstructor
public class DestroySourceThenDealDamageToControllerEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final DamageSupport damageSupport;
    private final DestructionSupport destructionSupport;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroySourceThenDealDamageToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroySourceThenDealDamageToControllerEffect) effect;
        UUID sourceId = entry.getSourcePermanentId();
        Permanent source = sourceId == null ? null : gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        Card sourceCard = source.getCard();
        Permanent sourceSnapshot = new Permanent(source);
        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));

        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                entry.getControllerId(),
                sourceCard.getName() + "'s ability",
                List.of(),
                null,
                sourceId);
        damageEntry.setSourcePermanentSnapshot(sourceSnapshot);

        destructionSupport.tryDestroyAndLog(gameData, source, entry.getCard().getName());

        if (!gameData.playerIds.contains(entry.getControllerId())
                || damageSupport.isDamageSourcePreventedWithLog(gameData, damageEntry)) {
            return;
        }

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, damage, damageEntry);
        damageSupport.dealDamageToPlayer(gameData, damageEntry, entry.getControllerId(), rawDamage);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
