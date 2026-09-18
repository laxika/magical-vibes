package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerHasAtLeastOpponents;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "TMC", collectorNumber = "80")
public class UndergrowthStadium extends Card {

    public UndergrowthStadium() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new NotCondition(new ControllerHasAtLeastOpponents(2)), new EntersTappedEffect()));

        // {T}: Add {B} or {G}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
