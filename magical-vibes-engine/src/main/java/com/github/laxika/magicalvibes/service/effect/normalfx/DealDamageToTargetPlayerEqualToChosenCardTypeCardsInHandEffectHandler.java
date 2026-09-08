package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEqualToChosenCardTypeCardsInHandEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Blood Oath's chosen-card-type hand reveal and damage. */
@Component
@RequiredArgsConstructor
public class DealDamageToTargetPlayerEqualToChosenCardTypeCardsInHandEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetPlayerEqualToChosenCardTypeCardsInHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetPlayerEqualToChosenCardTypeCardsInHandEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        if (gameData.chosenSpellCardType == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCardTypeChoice(gameData, entry.getControllerId());
            return;
        }

        CardType chosenCardType = gameData.chosenSpellCardType;
        gameData.chosenSpellCardType = null;
        gameData.rerunCurrentEffectAfterInteraction = false;

        playerInteractionSupport.resolveRevealHand(gameData, targetPlayerId);

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        List<Card> hand = gameData.playerHands.getOrDefault(targetPlayerId, List.of());
        int matchingCards = (int) hand.stream()
                .filter(card -> card.hasType(chosenCardType))
                .count();
        if (matchingCards == 0) {
            return;
        }

        int rawDamage = Math.multiplyExact(matchingCards, e.damagePerCard());
        int damage = gameQueryService.applyDamageMultiplier(gameData, rawDamage, entry);
        damageSupport.dealDamageToPlayer(gameData, entry, targetPlayerId, damage);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
