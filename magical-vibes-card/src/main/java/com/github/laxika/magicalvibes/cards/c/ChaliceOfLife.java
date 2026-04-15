package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "146")
public class ChaliceOfLife extends Card {

    public ChaliceOfLife() {
        // Set up back face
        ChaliceOfDeath backFace = new ChaliceOfDeath();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // {T}: You gain 1 life. Then if you have at least 10 life more than your
        // starting life total, transform this artifact.
        // Starting life total = 20, so threshold = 30.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new GainLifeEffect(1),
                        new ControllerLifeThresholdConditionalEffect(
                                GameData.STARTING_LIFE_TOTAL + 10,
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
