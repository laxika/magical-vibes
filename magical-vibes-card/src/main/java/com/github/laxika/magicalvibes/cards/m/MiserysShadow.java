package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "107")
public class MiserysShadow extends Card {

    public MiserysShadow() {
        addEffect(EffectSlot.STATIC, new ExileOpponentCreaturesInsteadOfDyingEffect());
        addActivatedAbility(new ActivatedAbility(false, "{1}", List.of(new BoostSelfEffect(1, 1)),
                "Misery's Shadow gets +1/+1 until end of turn."));
    }
}
