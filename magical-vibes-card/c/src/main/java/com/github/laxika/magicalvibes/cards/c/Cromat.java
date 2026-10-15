package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentInCombatWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "94")
public class Cromat extends Card {

    public Cromat() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{B}",
                List.of(new DestroyTargetPermanentEffect()),
                "{W}{B}: Destroy target creature blocking or blocked by Cromat.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentInCombatWithSourcePredicate()
                        )),
                        "Target must be a creature blocking or blocked by Cromat"
                )
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{R}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{U}{R}: Cromat gains flying until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{G}",
                List.of(new RegenerateEffect()),
                "{B}{G}: Regenerate Cromat."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{W}",
                List.of(new BoostSelfEffect(1, 1)),
                "{R}{W}: Cromat gets +1/+1 until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{U}",
                List.of(PutTargetOnTopOfLibraryEffect.self()),
                "{G}{U}: Put Cromat on top of its owner's library."
        ));
    }
}
