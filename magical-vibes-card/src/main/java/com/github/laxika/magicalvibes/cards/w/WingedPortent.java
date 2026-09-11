package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "89")
public class WingedPortent extends Card {

    public WingedPortent() {
        addCastingOption(AlternateHandCast.cleave("{4}{G}{U}", null));

        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastForAlternateCost(),
                new DrawCardEffect(new PermanentCount(
                        new PermanentIsCreaturePredicate(), CountScope.CONTROLLER))));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastForAlternateCost()),
                new DrawCardEffect(new PermanentCount(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasKeywordPredicate(Keyword.FLYING))),
                        CountScope.CONTROLLER))));
    }
}
