package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentFromTriggeringSpellEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves damage that is dealt by the spell that caused a trigger. */
@Component
@RequiredArgsConstructor
public class DealDamageToEachOpponentFromTriggeringSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final DamageSupport damageSupport;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachOpponentFromTriggeringSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        Card triggeringCard = findTriggeringCard(gameData, triggeringCardId);
        if (triggeringCard == null) {
            return;
        }

        StackEntryType spellType = triggeringCard.hasType(com.github.laxika.magicalvibes.model.CardType.INSTANT)
                ? StackEntryType.INSTANT_SPELL : StackEntryType.SORCERY_SPELL;
        StackEntry damageEntry = new StackEntry(
                spellType, triggeringCard, entry.getControllerId(),
                triggeringCard.getName(), List.of());
        int amount = Math.max(0, amountEvaluationService.evaluate(gameData,
                ((DealDamageToEachOpponentFromTriggeringSpellEffect) effect).amount(),
                AmountContext.forStackEntry(damageEntry, null)));
        if (amount <= 0 || damageSupport.isDamageSourcePreventedWithLog(gameData, damageEntry)) {
            return;
        }

        int damage = gameQueryService.applyDamageMultiplier(gameData, amount, damageEntry);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(damageEntry.getControllerId())) {
                damageSupport.dealDamageToPlayer(gameData, damageEntry, playerId, damage);
            }
        }
        gameOutcomeService.checkWinCondition(gameData);
    }

    private Card findTriggeringCard(GameData gameData, UUID cardId) {
        StackEntry spell = gameQueryService.findStackEntryByCardId(gameData, cardId);
        if (spell != null) {
            return spell.getCard();
        }
        Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
        return card != null ? card : gameQueryService.findCardInExileById(gameData, cardId);
    }
}
