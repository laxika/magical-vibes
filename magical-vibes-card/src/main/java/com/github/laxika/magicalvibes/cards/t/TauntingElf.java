package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;

@CardRegistration(set = "UDS", collectorNumber = "122")
@CardRegistration(set = "ONS", collectorNumber = "290")
public class TauntingElf extends Card {

    public TauntingElf() {
        addEffect(EffectSlot.STATIC, new MustBeBlockedByAllCreaturesEffect());
    }
}
