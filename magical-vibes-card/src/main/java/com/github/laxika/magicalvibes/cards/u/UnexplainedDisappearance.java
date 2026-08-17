package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GRN", collectorNumber = "56")
public class UnexplainedDisappearance extends Card {

    public UnexplainedDisappearance() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
        addEffect(EffectSlot.SPELL, new SurveilEffect(1));
    }
}
