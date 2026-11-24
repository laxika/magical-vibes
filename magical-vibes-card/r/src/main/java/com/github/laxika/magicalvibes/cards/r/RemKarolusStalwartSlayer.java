package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MID", collectorNumber = "235")
@CardRegistration(set = "DBL", collectorNumber = "235")
public class RemKarolusStalwartSlayer extends Card {

    public RemKarolusStalwartSlayer() {
        addStaticEffect(PreventDamageToRecipientsEffect.toYouAnd(Subjects.otherPermanentsYouControl()).fromSpells());
        addStaticEffect(new AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(1)
                .fromAnyonesSources().fromSpells());
    }
}
