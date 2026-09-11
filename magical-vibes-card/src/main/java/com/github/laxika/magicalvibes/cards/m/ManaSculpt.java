package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaEqualToTargetSpellManaSpentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SOS", collectorNumber = "57")
public class ManaSculpt extends Card {

    public ManaSculpt() {
        // Counter target spell. If you control a Wizard, add colorless mana equal to that spell's
        // actual mana spent at the beginning of your next main phase.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.WIZARD)),
                new RegisterDelayedManaEqualToTargetSpellManaSpentEffect(ManaColor.COLORLESS)));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
