package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTwoThenSacrificeSelfIfMilledCardsShareAllTypesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MillTwoThenSacrificeSelfIfMilledCardsShareAllTypesEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final SacrificeSelfEffectHandler sacrificeSelfEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillTwoThenSacrificeSelfIfMilledCardsShareAllTypesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> milled = graveyardService.resolveMillPlayerAndReturnAllMilledCards(
                gameData, entry.getControllerId(), 2);
        boolean sharedTypes = false;
        for (int i = 0; i < milled.size() && !sharedTypes; i++) {
            for (int j = i + 1; j < milled.size() && !sharedTypes; j++) {
                sharedTypes = cardTypes(milled.get(i)).equals(cardTypes(milled.get(j)));
            }
        }
        if (!sharedTypes) {
            return;
        }

        sacrificeSelfEffectHandler.resolve(gameData, entry, new SacrificeSelfEffect());
    }

    private Set<CardType> cardTypes(Card card) {
        EnumSet<CardType> types = EnumSet.noneOf(CardType.class);
        for (CardType type : CardType.values()) {
            if (card.hasType(type)) {
                types.add(type);
            }
        }
        return types;
    }
}
