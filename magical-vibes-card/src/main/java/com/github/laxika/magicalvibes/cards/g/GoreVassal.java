package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "7")
public class GoreVassal extends Card {

    public GoreVassal() {
        // Sacrifice Gore Vassal: Put a -1/-1 counter on target creature.
        // Then if that creature's toughness is 1 or greater, regenerate it.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new PutMinusOneMinusOneCounterOnTargetCreatureEffect(1, true)),
                "Sacrifice Gore Vassal: Put a -1/-1 counter on target creature. Then if that creature's toughness is 1 or greater, regenerate it.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
