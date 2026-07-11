package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "86")
public class BoldwyrIntimidator extends Card {

    public BoldwyrIntimidator() {
        // Cowards can't block Warriors.
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                new PermanentHasSubtypePredicate(CardSubtype.COWARD),
                new PermanentHasSubtypePredicate(CardSubtype.WARRIOR),
                "Cowards can't block Warriors"));

        // {R}: Target creature becomes a Coward until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{R}",
                List.of(new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.COWARD)),
                "{R}: Target creature becomes a Coward until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));

        // {2}{R}: Target creature becomes a Warrior until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{R}",
                List.of(new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.WARRIOR)),
                "{2}{R}: Target creature becomes a Warrior until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));
    }
}
