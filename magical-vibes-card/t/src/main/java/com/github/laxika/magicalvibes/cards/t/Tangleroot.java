package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaToActivePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "259")
public class Tangleroot extends Card {

    public Tangleroot() {
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                new AwardManaToActivePlayerEffect(ManaColor.GREEN, 1).to(Subjects.triggeringPlayer()));
    }
}
