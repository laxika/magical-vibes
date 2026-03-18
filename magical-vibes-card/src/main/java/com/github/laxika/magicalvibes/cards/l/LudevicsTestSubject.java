package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenTransformIfThresholdEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "64")
public class LudevicsTestSubject extends Card {

    public LudevicsTestSubject() {
        // Set up back face
        LudevicsAbomination backFace = new LudevicsAbomination();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // Defender is loaded from Scryfall.

        // {1}{U}: Put a hatchling counter on Ludevic's Test Subject.
        // Then if there are five or more hatchling counters on it, remove all of them and transform it.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{U}",
                List.of(new PutCounterOnSelfThenTransformIfThresholdEffect(CounterType.HATCHLING, 5)),
                "{1}{U}: Put a hatchling counter on Ludevic's Test Subject. Then if there are five or more hatchling counters on it, remove all of them and transform it."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "LudevicsAbomination";
    }
}
