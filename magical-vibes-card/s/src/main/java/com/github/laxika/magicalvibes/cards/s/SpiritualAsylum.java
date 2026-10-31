package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "23")
public class SpiritualAsylum extends Card {

    public SpiritualAsylum() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.SHROUD,
                GrantScope.OWN_PERMANENTS,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsLandPredicate()))));
        // OWN_PERMANENTS never includes the source, so Spiritual Asylum covers itself while it is a creature.
        addStaticEffect(new GrantKeywordEffect(
                Keyword.SHROUD, GrantScope.SELF, new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new SacrificeSelfEffect());
    }
}
