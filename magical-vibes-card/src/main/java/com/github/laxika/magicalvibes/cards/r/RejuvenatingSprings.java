package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerHasAtLeastOpponents;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "MSC", collectorNumber = "260")
@CardRegistration(set = "MSC", collectorNumber = "488")
@CardRegistration(set = "CMM", collectorNumber = "424")
@CardRegistration(set = "CMM", collectorNumber = "617")
@CardRegistration(set = "CMM", collectorNumber = "662")
@CardRegistration(set = "LTC", collectorNumber = "325")
public class RejuvenatingSprings extends Card {

    public RejuvenatingSprings() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new NotCondition(new ControllerHasAtLeastOpponents(2)), new EntersTappedEffect()));

        // {T}: Add {G} or {U}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
