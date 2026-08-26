package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "24")
public class MalachOfTheDawn extends Card {

    public MalachOfTheDawn() {
        addActivatedAbility(new ActivatedAbility(false, "{W}{W}{W}",
                List.of(new RegenerateEffect()),
                "{W}{W}{W}: Regenerate Malach of the Dawn."));
    }
}
