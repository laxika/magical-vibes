package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaSpent;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.NoteAmountEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;

@CardRegistration(set = "SOS", collectorNumber = "57")
public class ManaSculpt extends Card {

    public ManaSculpt() {
        // The mana spent is read while the spell is still on the stack, before the counter step.
        addSpellEffect(new NoteAmountEffect(new TargetSpellManaSpent()));
        addSpellEffect(new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.WIZARD)),
                RegisterDelayedStepTriggerEffect.atNextMainPhase(ActivePlayer.YOU,
                        new AwardManaEffect(ManaColor.COLORLESS, AmountThisWay.noted()))));
        addSpellEffect(new CounterSpellEffect());
    }
}
