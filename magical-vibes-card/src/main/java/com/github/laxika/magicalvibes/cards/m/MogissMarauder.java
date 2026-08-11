package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "97")
public class MogissMarauder extends Card {

    public MogissMarauder() {
        targetUpTo(new ColorManaSymbolsAmongControlledPermanents(ManaColor.BLACK),
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Targets must be creatures"),
                100)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantKeywordEffect(Set.of(Keyword.INTIMIDATE, Keyword.HASTE), GrantScope.TARGET));
    }
}
