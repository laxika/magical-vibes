package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerHasAtLeastOpponents;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "CMM", collectorNumber = "434")
@CardRegistration(set = "CMM", collectorNumber = "619")
@CardRegistration(set = "CMM", collectorNumber = "665")
public class TrainingCenter extends Card {

    public TrainingCenter() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new NotCondition(new ControllerHasAtLeastOpponents(2)), new EntersTappedEffect()));

        // {T}: Add {U} or {R}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
    }
}
