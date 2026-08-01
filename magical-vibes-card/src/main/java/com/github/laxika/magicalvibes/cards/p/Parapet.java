package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlashCastWithCleanupSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "VIS", collectorNumber = "14")
public class Parapet extends Card {

    public Parapet() {
        // Mirage flash clause: cast at instant speed, but sacrificed at next cleanup if cast when
        // a sorcery couldn't have been cast. Creatures you control get +0/+1.
        addEffect(EffectSlot.STATIC, new FlashCastWithCleanupSacrificeEffect());
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 1, GrantScope.OWN_CREATURES));
    }
}
