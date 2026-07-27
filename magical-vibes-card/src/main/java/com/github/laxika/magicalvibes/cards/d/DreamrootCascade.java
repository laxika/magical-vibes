package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;


@CardRegistration(set = "SOS", collectorNumber = "254")
@CardRegistration(set = "SOS", collectorNumber = "302")
public class DreamrootCascade extends Card {

    public DreamrootCascade() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(1, new PermanentIsLandPredicate()), new EntersTappedEffect()));

        // {T}: Add {G}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));

        // {T}: Add {U}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
