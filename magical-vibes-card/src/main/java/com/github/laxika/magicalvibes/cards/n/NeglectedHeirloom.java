package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AshmouthBlade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "INR", collectorNumber = "269")
@CardRegistration(set = "INR", collectorNumber = "473")
public class NeglectedHeirloom extends Card {

    public NeglectedHeirloom() {
        setBackFaceCard(new AshmouthBlade());

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));

        // When equipped creature transforms, transform this Equipment.
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_TRANSFORMS, new TransformSelfEffect());

        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "AshmouthBlade";
    }
}
