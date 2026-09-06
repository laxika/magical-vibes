package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "60")
public class MerfolkCoralsmith extends Card {

    public MerfolkCoralsmith() {
        addActivatedAbility(new ActivatedAbility(false, "{1}", List.of(new BoostSelfEffect(1, -1)),
                "{1}: This creature gets +1/-1 until end of turn."));
        addEffect(EffectSlot.ON_DEATH, new ScryEffect(2));
    }
}
