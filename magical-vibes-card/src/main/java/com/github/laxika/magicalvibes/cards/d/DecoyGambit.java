package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreaturesUnlessControllersDrawEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "C20", collectorNumber = "32")
public class DecoyGambit extends Card {

    public DecoyGambit() {
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        setTargetControllersFixedAtCast(true);
        target(TargetFilters.creatureAnOpponentControls(), 0, 99)
                .addEffect(EffectSlot.SPELL, new ReturnTargetCreaturesUnlessControllersDrawEffect());
    }
}
