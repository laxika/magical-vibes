package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;


@CardRegistration(set = "SOS", collectorNumber = "264")
@CardRegistration(set = "SOS", collectorNumber = "305")
@CardRegistration(set = "INR", collectorNumber = "286")
public class SundownPass extends Card {

    public SundownPass() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(1, new PermanentIsLandPredicate()), new EntersTappedEffect()));

        // {T}: Add {R}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        // {T}: Add {W}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
