package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ZEN", collectorNumber = "227")
public class TurntimberGrove extends Card {

    public TurntimberGrove() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostTargetCreatureEffect(1, 1));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
