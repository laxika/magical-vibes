package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlledDragonAsCast;
import com.github.laxika.magicalvibes.model.condition.RevealCardFromHandCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardFromHandCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "134")
public class DraconicRoar extends Card {

    public DraconicRoar() {
        addEffect(EffectSlot.SPELL, RevealCardFromHandCost.optional(
                new CardSubtypePredicate(CardSubtype.DRAGON), "Dragon"));
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new AnyOf(List.of(new RevealCardFromHandCostPaid(), new ControlledDragonAsCast())),
                new DealDamageToPlayersEffect(3, DamageRecipient.TARGET_PERMANENT_CONTROLLER)));
    }
}
