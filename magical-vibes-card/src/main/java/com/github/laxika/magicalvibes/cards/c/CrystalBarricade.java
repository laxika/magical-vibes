package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToCreaturesEffect;

@CardRegistration(set = "FDN", collectorNumber = "7")
public class CrystalBarricade extends Card {

    public CrystalBarricade() {
        // "You have hexproof."
        addEffect(EffectSlot.STATIC, new GrantControllerKeywordEffect(Keyword.HEXPROOF));

        // "Prevent all noncombat damage that would be dealt to other creatures you control."
        addEffect(EffectSlot.STATIC, PreventDamageToCreaturesEffect.otherCreaturesYouControl(true));
    }
}
