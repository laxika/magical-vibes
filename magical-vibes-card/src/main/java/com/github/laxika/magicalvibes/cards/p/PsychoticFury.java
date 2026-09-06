package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "68")
public class PsychoticFury extends Card {

    public PsychoticFury() {
        PermanentPredicate multicoloredCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsMulticoloredPredicate()));

        target(new PermanentPredicateTargetFilter(
                multicoloredCreature,
                "Target must be a multicolored creature"))
                .addEffect(EffectSlot.SPELL,
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET, multicoloredCreature))
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
