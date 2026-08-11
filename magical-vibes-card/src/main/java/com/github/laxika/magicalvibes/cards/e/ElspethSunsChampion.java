package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "9")
public class ElspethSunsChampion extends Card {

    public ElspethSunsChampion() {
        // +1: Create three 1/1 white Soldier creature tokens.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(CreateTokenEffect.whiteSoldier(3)),
                "+1: Create three 1/1 white Soldier creature tokens."
        ));

        // −3: Destroy all creatures with power 4 or greater.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(4)
                )))),
                "−3: Destroy all creatures with power 4 or greater."
        ));

        // −7: You get an emblem with "Creatures you control get +2/+2 and have flying."
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(
                                new StaticBoostEffect(2, 2, Set.of(Keyword.FLYING), GrantScope.OWN_CREATURES)
                        ),
                        "Creatures you control get +2/+2 and have flying."
                )),
                "−7: You get an emblem with \"Creatures you control get +2/+2 and have flying.\""
        ));
    }
}
