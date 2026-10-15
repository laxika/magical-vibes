package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamageToPlaneswalkersYouControlEffect;

import java.util.Map;

@CardRegistration(set = "MOC", collectorNumber = "70")
public class TheWesternCloud extends Card {

    public TheWesternCloud() {
        addEffect(EffectSlot.STATIC, new PreventAllDamageToCreaturesYouControlEffect(null));
        addEffect(EffectSlot.STATIC, new PreventFixedDamageToPlaneswalkersYouControlEffect(Integer.MAX_VALUE));

        addEffect(EffectSlot.CHAOS_TRIGGERED,
                CreateTokenEffect.ofTappedTreasureToken(3).withTokenEffects(Map.of(
                        EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MassDamageEffect(1, false, false, true, null))));
    }
}
