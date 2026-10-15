package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "27a")
@CardRegistration(set = "FEM", collectorNumber = "27b")
public class TidalFlats extends Card {

    public TidalFlats() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{U}",
                List.of(new GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect(
                        Keyword.FIRST_STRIKE,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsAttackingPredicate(),
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
                        )),
                        "{1}"
                )),
                "{U}{U}: For each attacking creature without flying, its controller may pay {1}."
        ));
    }
}
