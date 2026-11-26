package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "298")
public class Monsoon extends Card {
    public Monsoon() {
        // "That player" is the end-step player the trigger recorded; an Island already tapped isn't tapped this way.
        addTrigger(Triggers.endStep(ActivePlayer.ANY), SequenceEffect.of(
                new TapPermanentsEffect(Subjects.controlledBy(Subjects.triggeringPlayer(),
                        new PermanentHasSubtypePredicate(CardSubtype.ISLAND))),
                DealDamageEffect.to(Subjects.triggeringPlayer(), new AmountThisWay(StepKind.TAP))));
    }
}
