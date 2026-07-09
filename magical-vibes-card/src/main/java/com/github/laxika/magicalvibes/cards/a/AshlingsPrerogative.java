package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseManaValueParityOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesOfUnchosenParityEnterTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToCreaturesOfChosenParityEffect;

@CardRegistration(set = "LRW", collectorNumber = "150")
public class AshlingsPrerogative extends Card {

    public AshlingsPrerogative() {
        // "As this enchantment enters, choose odd or even. (Zero is even.)"
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseManaValueParityOnEnterEffect());
        // "Each creature with mana value of the chosen quality has haste."
        addEffect(EffectSlot.STATIC, new GrantKeywordToCreaturesOfChosenParityEffect(Keyword.HASTE));
        // "Each creature without mana value of the chosen quality enters tapped."
        addEffect(EffectSlot.STATIC, new CreaturesOfUnchosenParityEnterTappedEffect());
    }
}
