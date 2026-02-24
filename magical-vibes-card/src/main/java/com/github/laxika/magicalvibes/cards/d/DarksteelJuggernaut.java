package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledArtifactCountEffect;

@CardRegistration(set = "SOM", collectorNumber = "150")
public class DarksteelJuggernaut extends Card {

    public DarksteelJuggernaut() {
        addEffect(EffectSlot.STATIC, new PowerToughnessEqualToControlledArtifactCountEffect());
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
