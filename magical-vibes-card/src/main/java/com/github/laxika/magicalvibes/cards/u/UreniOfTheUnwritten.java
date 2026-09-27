package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "9")
public class UreniOfTheUnwritten extends Card {

    public UreniOfTheUnwritten() {
        CardEffect revealDragon = LookAtTopCardsEffect.mayPutMatchingOntoBattlefieldRestOnBottomRandom(
                8,
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(CardSubtype.DRAGON)
                )));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, revealDragon);
        addEffect(EffectSlot.ON_ATTACK, revealDragon);
    }
}
