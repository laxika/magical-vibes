package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AwokenDemon;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "107")
public class EcstaticAwakener extends Card {

    public EcstaticAwakener() {
        AwokenDemon backFace = new AwokenDemon();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // {2}{B}, Sacrifice another creature: Draw a card, then transform this creature.
        // Activate only once each turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "Sacrifice another creature"),
                        new DrawCardEffect(1),
                        new TransformSelfEffect()),
                "{2}{B}, Sacrifice another creature: Draw a card, then transform this creature. Activate only once each turn.",
                1
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "AwokenDemon";
    }
}
