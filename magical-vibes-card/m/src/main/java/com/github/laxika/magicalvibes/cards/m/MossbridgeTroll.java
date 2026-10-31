package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.RegeneratesIfWouldBeDestroyedEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "123")
public class MossbridgeTroll extends Card {

    public MossbridgeTroll() {
        // "If this creature would be destroyed, regenerate it." — always-on intrinsic regeneration.
        addStaticEffect(new RegeneratesIfWouldBeDestroyedEffect());

        // "Tap any number of untapped creatures you control other than this creature with total
        // power 10 or greater: This creature gets +20/+20 until end of turn."
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(CrewCost.tapCreaturesWithTotalPower(10), new BoostSelfEffect(20, 20)),
                "Tap any number of untapped creatures you control other than this creature with total power 10 or greater: This creature gets +20/+20 until end of turn."
        ));
    }
}
