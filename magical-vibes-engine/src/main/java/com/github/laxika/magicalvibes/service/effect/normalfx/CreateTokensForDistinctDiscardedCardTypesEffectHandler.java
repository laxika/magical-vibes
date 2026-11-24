package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForDistinctDiscardedCardTypesEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.EnumSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Creates one copy of the effect's token for each distinct card type among the cards the most recent discard step of
 * the resolution discarded ({@link StepKind#DISCARD}); none when it discarded nothing.
 */
@Component
@RequiredArgsConstructor
public class CreateTokensForDistinctDiscardedCardTypesEffectHandler
        implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensForDistinctDiscardedCardTypesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokensForDistinctDiscardedCardTypesEffect createTokens =
                (CreateTokensForDistinctDiscardedCardTypesEffect) effect;
        EnumSet<CardType> cardTypes = EnumSet.noneOf(CardType.class);
        // The cards the discard step discarded, wherever they went (a replacement may have put one elsewhere).
        for (UUID cardId : entry.getResolutionResults().outcome(StepKind.DISCARD).objectIds()) {
            Card card = gameQueryService.findCardById(gameData, cardId);
            if (card == null) {
                continue;
            }
            if (card.getType() != null) {
                cardTypes.add(card.getType());
            }
            cardTypes.addAll(card.getAdditionalTypes());
        }

        if (!cardTypes.isEmpty()) {
            CreateTokenEffect token = createTokens.tokenTemplate().withAmount(cardTypes.size());
            createTokenEffectHandler.resolveForController(gameData, entry, token,
                    entry.getControllerId());
        }
    }
}
