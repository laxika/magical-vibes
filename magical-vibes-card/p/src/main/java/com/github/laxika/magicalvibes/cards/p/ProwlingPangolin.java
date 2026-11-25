package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "163")
@CardRegistration(set = "EMA", collectorNumber = "104")
public class ProwlingPangolin extends Card {

    public ProwlingPangolin() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                UnlessPaysEffect.mayPay(Subjects.eachPlayer(),
                        new SacrificeMultiplePermanentsCost(2, new PermanentIsCreaturePredicate(), "creatures"),
                        new SacrificeSelfEffect())
                        .withPrompt("Sacrifice two creatures? If a player does, it is sacrificed.")
                        .payingTogether());
    }
}
