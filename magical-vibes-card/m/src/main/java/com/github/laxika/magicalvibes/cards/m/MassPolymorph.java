package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.ExileAllCreaturesYouControlThenRevealCreaturesToBattlefieldEffect;

@CardRegistration(set = "M11", collectorNumber = "64")
public class MassPolymorph extends Card {

    public MassPolymorph() {
        addSpellEffect(new ExileAllCreaturesYouControlThenRevealCreaturesToBattlefieldEffect());
    }
}
