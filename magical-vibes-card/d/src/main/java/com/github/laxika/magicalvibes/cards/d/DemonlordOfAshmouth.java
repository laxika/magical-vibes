package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "AVR", collectorNumber = "96")
public class DemonlordOfAshmouth extends Card {

    public DemonlordOfAshmouth() {
        // Flying and Undying are auto-loaded from Scryfall.

        // When this creature enters, exile it unless you sacrifice another creature.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                UnlessPaysEffect.unlessYouPay(new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "another creature"), new ExileTargetPermanentEffect(Subjects.source())));
    }
}
