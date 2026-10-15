package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SetChosenColorForTargetCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "INV", collectorNumber = "77")
public class SwayOfIllusion extends Card {

    public SwayOfIllusion() {
        target(TargetFilters.creature(), 0, 99)
                .addEffect(EffectSlot.SPELL, new SetChosenColorForTargetCreaturesUntilEndOfTurnEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
