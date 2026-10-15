package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerHasAtLeastOpponents;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "CMM", collectorNumber = "436")
@CardRegistration(set = "CMM", collectorNumber = "621")
@CardRegistration(set = "CMM", collectorNumber = "667")
public class VaultOfChampions extends Card {

    public VaultOfChampions() {
        // This land enters tapped unless you have two or more opponents.
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new NotCondition(new ControllerHasAtLeastOpponents(2)),
                new EntersTappedEffect()));

        // {T}: Add {W} or {B}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
