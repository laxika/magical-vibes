package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "243")
public class MemorialToGenius extends Card {

    public MemorialToGenius() {
        // Memorial to Genius enters the battlefield tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        // {T}: Add {U}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLUE));
        // {4}{U}, {T}, Sacrifice Memorial to Genius: Draw two cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{4}{U}, {T}, Sacrifice Memorial to Genius: Draw two cards."
        ));
    }
}
