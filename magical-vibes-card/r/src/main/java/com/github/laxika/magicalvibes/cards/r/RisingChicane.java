package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerIsNotStartingPlayer;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YDFT", collectorNumber = "30")
public class RisingChicane extends Card {

    public RisingChicane() {
        addStaticEffect(new ConditionalReplacementEffect(
                new NotCondition(new ControllerIsNotStartingPlayer()), new EntersTappedEffect()));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new MaxSpeed()),
                new AnimatePermanentsEffect(2, 2, List.of(CardSubtype.CONSTRUCT), Set.of()));
    }
}
