package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachChosenCardTypeCardInTargetHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves chosen-card-type hand reveals that create tokens for matching cards. */
@Component
@RequiredArgsConstructor
public class CreateTokensForEachChosenCardTypeCardInTargetHandEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensForEachChosenCardTypeCardInTargetHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokensForEachChosenCardTypeCardInTargetHandEffect) effect;
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

        List<Card> hand = gameData.playerHands.getOrDefault(targetPlayerId, List.of());
        int matchingCards = (int) hand.stream()
                .filter(card -> card.hasType(chosenCardType))
                .count();
        if (matchingCards == 0) {
            return;
        }

        var source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int tokensPerCard = amountEvaluationService.evaluate(gameData, e.tokenTemplate().amount(),
                AmountContext.forStackEntry(entry, source));
        if (tokensPerCard <= 0) {
            return;
        }

        int totalTokens = Math.multiplyExact(matchingCards, tokensPerCard);
        createTokenEffectHandler.resolveForController(gameData, entry,
                e.tokenTemplate().withAmount(totalTokens), entry.getControllerId());
    }
}
