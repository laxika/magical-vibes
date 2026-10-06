package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "84")
public class AscendFromAvernus extends Card {

    public AscendFromAvernus() {
        CardAnyOfPredicate creatureOrPlaneswalker = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.PLANESWALKER)));
        addEffect(EffectSlot.SPELL, new ReturnCardsFromControllerGraveyardToBattlefieldEffect(
                new CardAllOfPredicate(List.of(creatureOrPlaneswalker, new CardMaxManaValueXPredicate())),
                new Fixed(Integer.MAX_VALUE), true));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
