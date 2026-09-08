package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEqualToChosenColorCardsInHandEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a chosen- or fixed-color hand reveal and damage. */
@Component
@RequiredArgsConstructor
public class DealDamageToTargetPlayerEqualToChosenColorCardsInHandEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetPlayerEqualToChosenColorCardsInHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        DealDamageToTargetPlayerEqualToChosenColorCardsInHandEffect colorEffect =
                (DealDamageToTargetPlayerEqualToChosenColorCardsInHandEffect) effect;
        if (colorEffect.fixedColor() == null && gameData.chosenSpellColor == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellColorChoice(gameData, entry.getControllerId());
            return;
        }

        CardColor chosenColor = colorEffect.fixedColor() != null
                ? colorEffect.fixedColor()
                : gameData.chosenSpellColor;
        if (colorEffect.fixedColor() == null) {
            gameData.chosenSpellColor = null;
        }
        gameData.rerunCurrentEffectAfterInteraction = false;

        playerInteractionSupport.resolveRevealHand(gameData, targetPlayerId);

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        List<Card> hand = gameData.playerHands.getOrDefault(targetPlayerId, List.of());
        int matchingCards = (int) hand.stream()
                .filter(card -> card.getColors().contains(chosenColor))
                .count();
        if (matchingCards == 0) {
            return;
        }

        int damage = gameQueryService.applyDamageMultiplier(gameData, matchingCards, entry);
        damageSupport.dealDamageToPlayer(gameData, entry, targetPlayerId, damage);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
