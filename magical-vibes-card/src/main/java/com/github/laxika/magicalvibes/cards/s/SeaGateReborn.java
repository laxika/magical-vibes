package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeOrEntersTappedEffect;

public class SeaGateReborn extends Card {

    public SeaGateReborn() {
        addEffect(EffectSlot.STATIC, new MayPayLifeOrEntersTappedEffect(3));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
