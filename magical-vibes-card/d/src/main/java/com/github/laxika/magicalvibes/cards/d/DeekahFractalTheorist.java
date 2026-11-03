package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "193")
@CardRegistration(set = "C21", collectorNumber = "26")
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
        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery), createFractal);
        addTrigger(Triggers.youCopySpell().withFilter(instantOrSorcery), createFractal);

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
