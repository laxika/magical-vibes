package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "146")
@CardRegistration(set = "INR", collectorNumber = "257")
@CardRegistration(set = "INR", collectorNumber = "471")
public class ChaliceOfLife extends Card {

    public ChaliceOfLife() {
        setBackFaceCard(new ChaliceOfDeath());

        // {T}: You gain 1 life. Then if you have at least 10 life more than your
        // starting life total, transform this artifact.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new GainLifeEffect(1),
                        new ConditionalEffect(
                                new ControllerLifeAtLeast(10, true),
                                new TransformSelfEffect()
                        )
                ),
                "{T}: You gain 1 life. Then if you have at least 10 life more than your starting life total, transform this artifact."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ChaliceOfDeath";
    }
}
