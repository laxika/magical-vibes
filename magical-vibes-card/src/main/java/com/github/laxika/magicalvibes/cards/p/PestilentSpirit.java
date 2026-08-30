package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantDeathtouchToControllerSpellsEffect;

@CardRegistration(set = "RNA", collectorNumber = "81")
public class PestilentSpirit extends Card {

    public PestilentSpirit() {
        addEffect(EffectSlot.STATIC, new GrantDeathtouchToControllerSpellsEffect());
    }
}
