package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerHasAtLeastOpponents;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "MSC", collectorNumber = "268")
@CardRegistration(set = "MSC", collectorNumber = "495")
public class SpectatorSeating extends Card {

    public SpectatorSeating() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new NotCondition(new ControllerHasAtLeastOpponents(2)), new EntersTappedEffect()));

        // {T}: Add {R} or {W}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
