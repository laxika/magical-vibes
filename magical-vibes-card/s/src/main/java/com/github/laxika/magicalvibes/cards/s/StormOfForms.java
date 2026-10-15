package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForEachCounterKindEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NCC", collectorNumber = "32")
@CardRegistration(set = "NCC", collectorNumber = "133")
public class StormOfForms extends Card {

    public StormOfForms() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellForEachCounterKindEffect());
    }
}
