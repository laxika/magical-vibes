package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AbolisherOfBloodlines;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsSequenceCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "111")
public class VoldarenPariah extends Card {

    public VoldarenPariah() {
        setBackFaceCard(new AbolisherOfBloodlines());
        addCastingOption(new MadnessCast("{B}{B}{B}"));

        var anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentsSequenceCost(
                                List.of(anotherCreature, anotherCreature, anotherCreature),
                                List.of("another creature", "another creature", "another creature")
                        ),
                        new TransformSelfEffect()
                ),
                "Sacrifice three other creatures: Transform this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "AbolisherOfBloodlines";
    }
}
