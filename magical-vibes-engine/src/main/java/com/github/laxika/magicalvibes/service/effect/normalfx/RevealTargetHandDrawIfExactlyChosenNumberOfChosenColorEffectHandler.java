package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTargetHandDrawIfExactlyChosenNumberOfChosenColorEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves Scrying Glass's reveal-and-guess ability. */
@Component
@RequiredArgsConstructor
public class RevealTargetHandDrawIfExactlyChosenNumberOfChosenColorEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTargetHandDrawIfExactlyChosenNumberOfChosenColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        playerInteractionSupport.resolveRevealHand(gameData, entry.getTargetId());

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || source.getChosenColor() == null || source.getChosenNumber() <= 0) {
            return;
        }

        CardColor chosenColor = source.getChosenColor();
        List<Card> hand = gameData.playerHands.getOrDefault(entry.getTargetId(), List.of());
        long matchingCards = hand.stream()
                .filter(card -> card.getColors().contains(chosenColor))
                .count();
        if (matchingCards == source.getChosenNumber()) {
            playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), 1);
        }
    }
}
