package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "XLN", collectorNumber = "71")
@CardRegistration(set = "GN2", collectorNumber = "24")
@CardRegistration(set = "NCC", collectorNumber = "231")
public class RiversRebuke extends Card {

    public RiversRebuke() {
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.permanentsTargetPlayerControls(
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
