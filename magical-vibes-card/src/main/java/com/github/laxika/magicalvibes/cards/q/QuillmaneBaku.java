package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "48")
public class QuillmaneBaku extends Card {

    public QuillmaneBaku() {
        // Whenever you cast a Spirit or Arcane spell, you may put a ki counter on this creature.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.SPIRIT),
                                new CardSubtypePredicate(CardSubtype.ARCANE))),
                        List.of(new MayEffect(new PutCountersOnSelfEffect(CounterType.KI),
                                "Put a ki counter on this creature?"))));

        // {1}, {T}, Remove X ki counters from this creature: Return target creature with mana value
        // X or less to its owner's hand. X is announced at activation (capped by the ki counters
        // present) and bounds the target's mana value via PermanentMaxManaValueXPredicate.
        addActivatedAbility(new ActivatedAbility(true, "{1}",
                List.of(new RemoveXCountersFromSourceCost(CounterType.KI),
                        ReturnToHandEffect.target()),
                "{1}, {T}, Remove X ki counters from this creature: Return target creature with mana "
                        + "value X or less to its owner's hand.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentMaxManaValueXPredicate())),
                        "Target must be a creature with mana value X or less.")));
    }
}
