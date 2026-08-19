package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;

@CardRegistration(set = "INR", collectorNumber = "268")
@CardRegistration(set = "INR", collectorNumber = "445")
@CardRegistration(set = "AER", collectorNumber = "164")
public class MetallicMimic extends Card {

    public MetallicMimic() {
        // As this creature enters, choose a creature type.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        // This creature is the chosen type in addition to its other types.
        addEffect(EffectSlot.STATIC, GrantChosenSubtypeToOwnCreaturesEffect.toSelf());

        // Each other creature you control of the chosen type enters with an additional +1/+1 counter on it.
        addEffect(EffectSlot.STATIC, ControlledCreaturesEnterWithAdditionalCountersEffect.ofChosenSubtype(1));
    }
}
