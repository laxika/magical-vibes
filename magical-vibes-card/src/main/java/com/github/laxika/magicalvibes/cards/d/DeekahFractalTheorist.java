package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "193")
public class DeekahFractalTheorist extends Card {

    public DeekahFractalTheorist() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        CardEffect createFractal = new CreateXTokenWithXCountersEffect(
                "Fractal",
                0,
                0,
                CardColor.GREEN,
                Set.of(CardColor.GREEN, CardColor.BLUE),
                List.of(CardSubtype.FRACTAL),
                CounterType.PLUS_ONE_PLUS_ONE,
                new EventValue());

        // Magecraft — Whenever you cast or copy an instant or sorcery spell, create a Fractal
        // token and put +1/+1 counters on it equal to that spell's mana value.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(instantOrSorcery, List.of(createFractal)));
        addEffect(EffectSlot.ON_CONTROLLER_COPIES_SPELL,
                new SpellCopyTriggerEffect(instantOrSorcery, List.of(createFractal)));

        PermanentAllOfPredicate creatureToken = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsTokenPredicate()));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{U}",
                List.of(new MakeCreatureUnblockableEffect()),
                "{3}{U}: Target creature token can't be blocked this turn.",
                new PermanentPredicateTargetFilter(creatureToken, "Target must be a creature token")
        ));
    }
}
