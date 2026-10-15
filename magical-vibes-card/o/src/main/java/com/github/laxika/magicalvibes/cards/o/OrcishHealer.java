package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventTargetCreatureRegenerationThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "208")
public class OrcishHealer extends Card {

    public OrcishHealer() {
        addActivatedAbility(new ActivatedAbility(true, "{R}{R}",
                List.of(new PreventTargetCreatureRegenerationThisTurnEffect()),
                "{R}{R}, {T}: Target creature can't be regenerated this turn."));

        addActivatedAbility(new ActivatedAbility(true, "{B}{B}{R}",
                List.of(new RegenerateEffect(true)),
                "{B}{B}{R}, {T}: Regenerate target black or green creature.",
                blackOrGreenCreature()));

        addActivatedAbility(new ActivatedAbility(true, "{R}{G}{G}",
                List.of(new RegenerateEffect(true)),
                "{R}{G}{G}, {T}: Regenerate target black or green creature.",
                blackOrGreenCreature()));
    }

    private static PermanentPredicateTargetFilter blackOrGreenCreature() {
        return new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK, CardColor.GREEN)))),
                "Target must be a black or green creature");
    }
}
