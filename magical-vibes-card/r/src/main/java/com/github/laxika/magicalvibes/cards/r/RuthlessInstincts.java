package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FRF", collectorNumber = "136")
public class RuthlessInstincts extends Card {

    public RuthlessInstincts() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target nonattacking creature gains reach and deathtouch until end of turn. Untap it",
                        List.of(
                                new GrantKeywordEffect(Set.of(Keyword.REACH, Keyword.DEATHTOUCH), GrantScope.TARGET),
                                new UntapPermanentsEffect(TapUntapScope.TARGET)
                        ),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentIsAttackingPredicate())
                                )),
                                "Target must be a nonattacking creature."
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Target attacking creature gets +2/+2 and gains trample until end of turn",
                        List.of(
                                new BoostTargetCreatureEffect(2, 2),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)
                        ),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsAttackingPredicate()
                                )),
                                "Target must be an attacking creature."
                        )
                )
        )));
    }
}
