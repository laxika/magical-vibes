package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "193")
public class IxallisKeeper extends Card {

    public IxallisKeeper() {
        // {7}{G}, {T}, Sacrifice Ixalli's Keeper: Target creature gets +5/+5 and gains trample until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new BoostTargetCreatureEffect(5, 5),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)
                ),
                "{7}{G}, {T}, Sacrifice Ixalli's Keeper: Target creature gets +5/+5 and gains trample until end of turn.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature")
        ));
    }
}
