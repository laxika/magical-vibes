package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "206")
public class HyldaOfTheIcyCrown extends Card {

    public HyldaOfTheIcyCrown() {
        addEffect(EffectSlot.ON_CONTROLLER_TAPS_OPPONENT_PERMANENT,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(),
                        new MayPayManaEffect("{1}", new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Create a 4/4 white and blue Elemental creature token",
                                        new CreateTokenEffect("Elemental", 4, 4, CardColor.WHITE,
                                                Set.of(CardColor.WHITE, CardColor.BLUE),
                                                List.of(CardSubtype.ELEMENTAL))),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Put a +1/+1 counter on each creature you control",
                                        new PutCounterOnEachControlledPermanentEffect(
                                                CounterType.PLUS_ONE_PLUS_ONE, 1,
                                                new PermanentIsCreaturePredicate())),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Scry 2, then draw a card",
                                        SequenceEffect.of(new ScryEffect(2), new DrawCardEffect(1)))
                        )), "Pay {1}?")
                ));
    }
}
