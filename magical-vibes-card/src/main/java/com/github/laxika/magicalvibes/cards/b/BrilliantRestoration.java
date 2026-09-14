package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "7")
public class BrilliantRestoration extends Card {

    public BrilliantRestoration() {
        addEffect(EffectSlot.SPELL, new ReturnCardsFromControllerGraveyardToBattlefieldEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.ENCHANTMENT)
                )), Integer.MAX_VALUE));
    }
}
