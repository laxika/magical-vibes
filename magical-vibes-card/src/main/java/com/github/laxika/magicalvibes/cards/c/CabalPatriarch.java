package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "120")
public class CabalPatriarch extends Card {

    public CabalPatriarch() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new SacrificeCreatureCost(), new BoostTargetCreatureEffect(-2, -2)),
                "{2}{B}, Sacrifice a creature: Target creature gets -2/-2 until end of turn.",
                creatureTargetFilter()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new ExileCardFromGraveyardCost(CardType.CREATURE),
                        new BoostTargetCreatureEffect(-2, -2)),
                "{2}{B}, Exile a creature card from your graveyard: Target creature gets -2/-2 until end of turn.",
                creatureTargetFilter()
        ));
    }

    private static PermanentPredicateTargetFilter creatureTargetFilter() {
        return new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature");
    }
}
