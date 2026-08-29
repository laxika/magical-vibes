package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeDayAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "20")
public class GavonyDawnguard extends Card {

    public GavonyDawnguard() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeDayAsEntersEffect());
        addEffect(EffectSlot.ON_DAY_NIGHT_CHANGE,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(4,
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardMaxManaValuePredicate(3)
                        ))));
    }
}
