package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "139")
public class WargTactics extends Card {

    public WargTactics() {
        var flyingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasKeywordPredicate(Keyword.FLYING)
        ));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature with flying",
                        new DestroyTargetPermanentEffect(flyingCreature),
                        new PermanentPredicateTargetFilter(flyingCreature, "Target must be a creature with flying")),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on target creature you control. It gains trample and hexproof until end of turn",
                        List.of(
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET),
                                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.TARGET)
                        ),
                        TargetFilters.creatureYouControl())
        )));
    }
}
