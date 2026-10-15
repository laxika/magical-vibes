package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEC", collectorNumber = "25")
@CardRegistration(set = "NEC", collectorNumber = "65")
public class ConcordWithTheKami extends Card {

    public ConcordWithTheKami() {
        var creatureWithCounter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasCountersPredicate(CounterType.ANY)));
        var targetCreatureWithCounter = new PermanentPredicateTargetFilter(
                creatureWithCounter, "Target must be a creature with a counter on it.");
        var enchantedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsEnchantedPredicate()));
        var equippedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsEquippedPredicate()));
        var spiritToken = new CreateTokenEffect(
                "Spirit", 1, 1, null, List.of(CardSubtype.SPIRIT), Set.of(), Set.of());

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ChooseOneAtTriggerTimeEffect(
                ChooseOneEffect.oneOrMore(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Put a +1/+1 counter on target creature with a counter on it.",
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                targetCreatureWithCounter),
                        new ChooseOneEffect.ChooseOneOption(
                                "Draw a card if you control an enchanted creature.",
                                new ConditionalEffect(new ControlsPermanent(enchantedCreature),
                                        new DrawCardEffect())),
                        new ChooseOneEffect.ChooseOneOption(
                                "Create a 1/1 colorless Spirit creature token if you control an equipped creature.",
                                new ConditionalEffect(new ControlsPermanent(equippedCreature), spiritToken))
                ))));
    }
}
