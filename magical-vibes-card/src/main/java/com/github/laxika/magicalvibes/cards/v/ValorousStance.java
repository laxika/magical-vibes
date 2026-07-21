package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "48")
public class ValorousStance extends Card {

    public ValorousStance() {
        // Choose one —
        // • Target creature gains indestructible until end of turn.
        // • Destroy target creature with toughness 4 or greater.
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gains indestructible until end of turn",
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature."
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature with toughness 4 or greater",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentToughnessAtLeastPredicate(4)
                                )),
                                "Target must be a creature with toughness 4 or greater."
                        )
                )
        )));
    }
}
