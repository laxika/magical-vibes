package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtFaceDownAttackingOrBlockingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "DTK", collectorNumber = "197")
public class RevealingWind extends Card {

    public RevealingWind() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombat());
        addEffect(EffectSlot.SPELL, new LookAtFaceDownAttackingOrBlockingCreaturesEffect());
    }
}
