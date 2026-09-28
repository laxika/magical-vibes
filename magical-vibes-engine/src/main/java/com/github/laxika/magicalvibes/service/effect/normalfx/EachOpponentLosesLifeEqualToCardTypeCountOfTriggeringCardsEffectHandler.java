package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

/** Resolves the card-type count drain used by Polluted Cistern. */
@Component
@RequiredArgsConstructor
public class EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffectHandler
        implements NormalEffectHandlerBean {

    private final LoseLifeEffectHandler loseLifeEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var triggeringEffect = (EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect) effect;
        EnumSet<CardType> cardTypes = EnumSet.noneOf(CardType.class);
        for (var card : triggeringEffect.triggeringCards()) {
            for (CardType cardType : CardType.values()) {
                if (card.hasType(cardType)) {
                    cardTypes.add(cardType);
                }
            }
        }

        loseLifeEffectHandler.resolve(gameData, entry,
                new LoseLifeEffect(cardTypes.size(), LoseLifeRecipient.EACH_OPPONENT));
    }
}
