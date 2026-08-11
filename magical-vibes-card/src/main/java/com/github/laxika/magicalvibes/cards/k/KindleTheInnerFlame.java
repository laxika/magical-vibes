package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BeholdCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "147")
public class KindleTheInnerFlame extends Card {

    public KindleTheInnerFlame() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new CreateTokenCopyOfTargetPermanentEffect(true, false, true));
        addCastingOption(new FlashbackCast("{1}{R}"));
        addEffect(EffectSlot.SPELL, new BeholdCost(CardSubtype.ELEMENTAL, 3));
    }
}
