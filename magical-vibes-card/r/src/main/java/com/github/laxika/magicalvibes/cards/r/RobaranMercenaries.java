package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "DMC", collectorNumber = "22")
@CardRegistration(set = "DMC", collectorNumber = "72")
public class RobaranMercenaries extends Card {

    public RobaranMercenaries() {
        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfOwnCreaturesEffect(
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));
    }
}
