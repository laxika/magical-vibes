package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "ZNR", collectorNumber = "9")
public class DauntlessUnity extends Card {

    public DauntlessUnity() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{1}{W}"));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Kicked(),
                new BoostAllOwnCreaturesEffect(1, 1),
                new BoostAllOwnCreaturesEffect(2, 1)));
    }
}
