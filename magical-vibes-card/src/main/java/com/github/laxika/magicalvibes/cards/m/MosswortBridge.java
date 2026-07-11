package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayImprintedCardWithoutPayingManaCostEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "270")
public class MosswortBridge extends Card {

    public MosswortBridge() {
        // Hideaway 4 — when this enters, look at the top four cards, exile one face down, rest on bottom.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ImprintFromTopCardsEffect(4));
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        // {T}: Add {G}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
        // {G}, {T}: You may play the exiled card without paying its mana cost if creatures you control have total power 10 or greater.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new ConditionalEffect(new ControlledCreaturesTotalPowerAtLeast(10),
                        new PlayImprintedCardWithoutPayingManaCostEffect())),
                "{G}, {T}: You may play the exiled card without paying its mana cost if creatures you control have total power 10 or greater."
        ));
    }
}
