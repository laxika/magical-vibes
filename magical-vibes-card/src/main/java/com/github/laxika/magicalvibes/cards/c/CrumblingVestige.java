package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "OGW", collectorNumber = "170")
public class CrumblingVestige extends Card {

    public CrumblingVestige() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AwardAnyColorManaEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
