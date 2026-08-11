package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "36")
public class SepharaSkysBlade extends Card {

    public SepharaSkysBlade() {
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{W}"),
                new TapUntappedPermanentsCost(4, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING)))))));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.INDESTRUCTIBLE,
                GrantScope.OWN_CREATURES,
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
