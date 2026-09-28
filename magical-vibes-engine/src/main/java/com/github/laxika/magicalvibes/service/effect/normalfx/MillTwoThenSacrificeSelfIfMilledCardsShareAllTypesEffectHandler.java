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
        List<Card> milled = graveyardService.resolveMillPlayer(gameData, entry.getControllerId(), 2);
        if (milled.size() < 2 || !cardTypes(milled.get(0)).equals(cardTypes(milled.get(1)))) {
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
