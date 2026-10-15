package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GalianBeast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEqualToDyingPowerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "FIN", collectorNumber = "125")
@CardRegistration(set = "FIN", collectorNumber = "383")
@CardRegistration(set = "FIN", collectorNumber = "454")
@CardRegistration(set = "FIN", collectorNumber = "528")
public class VincentValentine extends Card {

    public VincentValentine() {
        setBackFaceCard(new GalianBeast());

        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES,
                new PutCountersOnSourceEqualToDyingPowerEffect(1, 1, false));
        addEffect(EffectSlot.ON_ATTACK,
                new MayEffect(new TransformSelfEffect(), "Transform Vincent Valentine?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "GalianBeast";
    }
}
