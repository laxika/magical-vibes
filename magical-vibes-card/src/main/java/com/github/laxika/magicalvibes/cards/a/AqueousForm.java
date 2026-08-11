package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "39")
public class AqueousForm extends Card {

    public AqueousForm() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new CantBeBlockedEffect())
                .addEffect(EffectSlot.ON_ATTACK, new ScryEffect(1));
    }
}
