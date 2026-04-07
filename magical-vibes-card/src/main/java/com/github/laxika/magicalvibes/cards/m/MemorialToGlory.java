package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "244")
public class MemorialToGlory extends Card {

    public MemorialToGlory() {
        // Memorial to Glory enters the battlefield tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        // {T}: Add {W}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.WHITE));
        // {3}{W}, {T}, Sacrifice Memorial to Glory: Create two 1/1 white Soldier creature tokens.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{W}",
                List.of(new SacrificeSelfCost(), CreateTokenEffect.whiteSoldier(2)),
                "{3}{W}, {T}, Sacrifice Memorial to Glory: Create two 1/1 white Soldier creature tokens."
        ));
    }
}
