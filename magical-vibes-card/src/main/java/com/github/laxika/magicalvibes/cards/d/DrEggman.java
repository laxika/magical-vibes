package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentFacesVillainousChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2084")
public class DrEggman extends Card {

    public DrEggman() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, SequenceEffect.of(
                new DrawCardEffect(1),
                new EachOpponentFacesVillainousChoiceEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.CONSTRUCT),
                                new CardSubtypePredicate(CardSubtype.ROBOT),
                                new CardSubtypePredicate(CardSubtype.VEHICLE))),
                        "Construct, Robot, or Vehicle")));
    }
}
