package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MMQ", collectorNumber = "106")
public class Statecraft extends Card {

    public Statecraft() {
        addStaticEffect(PreventDamageToRecipientsEffect.to(Subjects.creaturesYouControl()).combatOnly());
        addStaticEffect(PreventDamageToRecipientsEffect.dealtBy(new PermanentIsCreaturePredicate())
                .fromSourcesYouControl().combatOnly());
    }
}
