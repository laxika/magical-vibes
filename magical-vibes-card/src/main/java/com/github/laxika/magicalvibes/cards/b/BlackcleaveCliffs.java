package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;


@CardRegistration(set = "SOM", collectorNumber = "224")
public class BlackcleaveCliffs extends Card {

    public BlackcleaveCliffs() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCount(3, new PermanentIsLandPredicate()), new EntersTappedEffect()));

        // {T}: Add {B}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        // {T}: Add {R}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
    }
}
