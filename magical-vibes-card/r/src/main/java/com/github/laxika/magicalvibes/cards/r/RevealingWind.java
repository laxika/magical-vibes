package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtFaceDownAttackingOrBlockingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "DTK", collectorNumber = "197")
public class RevealingWind extends Card {

    public RevealingWind() {
        addSpellEffect(PreventDamageEffect.allCombat());
        addSpellEffect(new LookAtFaceDownAttackingOrBlockingCreaturesEffect());
    }
}
