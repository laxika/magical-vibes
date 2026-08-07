package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "INR", collectorNumber = "271")
public class StitchersGraft extends Card {

    public StitchersGraft() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 3, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_ATTACK, new SkipNextUntapEffect(TapUntapScope.ENCHANTED));
        addEffect(EffectSlot.STATIC, new SacrificeOnUnattachEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
