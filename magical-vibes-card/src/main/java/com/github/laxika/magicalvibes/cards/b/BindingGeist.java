package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SpectralBinding;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VOW", collectorNumber = "48")
public class BindingGeist extends Card {

    public BindingGeist() {
        setBackFaceCard(new SpectralBinding());

        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ATTACK, new BoostTargetCreatureEffect(-2, 0));

        addCastingOption(new DisturbCast("{1}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "SpectralBinding";
    }
}
