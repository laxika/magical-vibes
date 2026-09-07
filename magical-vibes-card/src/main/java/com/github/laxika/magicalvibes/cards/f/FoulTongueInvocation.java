package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlledDragonAsCast;
import com.github.laxika.magicalvibes.model.condition.RevealCardFromHandCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardFromHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "102")
public class FoulTongueInvocation extends Card {

    public FoulTongueInvocation() {
        addEffect(EffectSlot.SPELL, RevealCardFromHandCost.optional(
                new CardSubtypePredicate(CardSubtype.DRAGON), "Dragon"));
        addEffect(EffectSlot.SPELL,
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                        SacrificeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new AnyOf(List.of(new RevealCardFromHandCostPaid(), new ControlledDragonAsCast())),
                new GainLifeEffect(4)));
    }
}
