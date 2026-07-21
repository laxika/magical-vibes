package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "8")
public class DesertsHold extends Card {

    public DesertsHold() {
        // Enchant creature
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // When this Aura enters, if you control a Desert or there is a Desert card in
                // your graveyard, you gain 3 life.
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                        new AnyOf(List.of(
                                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DESERT)),
                                new GraveyardCardThreshold(1, new CardSubtypePredicate(CardSubtype.DESERT))
                        )),
                        new GainLifeEffect(3)));

        // Enchanted creature can't attack or block, and its activated abilities can't be activated.
        addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect());
        addEffect(EffectSlot.STATIC, new EnchantedCreatureCantActivateAbilitiesEffect());
    }
}
