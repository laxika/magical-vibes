package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAllCardsOfSacrificedCreatureColorsEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandAndDiscardMatchingCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.CardRevealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves Mind Extraction's color-dependent hand discard. */
@Component
@RequiredArgsConstructor
public class DiscardAllCardsOfSacrificedCreatureColorsEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final RevealHandAndDiscardMatchingCardsEffectHandler discardHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardAllCardsOfSacrificedCreatureColorsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card sacrificed = entry.getSacrificedCardSnapshot();
        if (sacrificed == null) {
            return;
        }

        if (sacrificed.getColors().isEmpty()) {
            cardRevealService.revealHandToAllPlayers(gameData, entry.getTargetId());
            return;
        }

        CardAnyOfPredicate sharedColor = new CardAnyOfPredicate(sacrificed.getColors().stream()
                .map(CardColorPredicate::new)
                .map(predicate -> (CardPredicate) predicate)
                .toList());
        discardHandler.resolve(gameData, entry, new RevealHandAndDiscardMatchingCardsEffect(sharedColor));
    }
}
