package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MKM", collectorNumber = "432")
public class VojaJawsOfTheConclave extends Card {

    public VojaJawsOfTheConclave() {
        PermanentCount elvesYouControl =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ELF), CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ATTACK, new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, elvesYouControl, new PermanentIsCreaturePredicate()));

        PermanentCount wolvesYouControl =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.WOLF), CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ATTACK, new DrawCardEffect(wolvesYouControl));
    }
}
