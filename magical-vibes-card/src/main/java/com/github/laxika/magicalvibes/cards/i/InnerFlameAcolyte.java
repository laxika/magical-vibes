package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "181")
public class InnerFlameAcolyte extends Card {

    public InnerFlameAcolyte() {
        // Evoke {R}: cast for the alternate cost instead of the mana cost; it's sacrificed on entry.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{R}"))));

        // When this creature enters, target creature gets +2/+0 and gains haste until end of turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostTargetCreatureEffect(2, 0))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));

        // Evoke sacrifice: if it was cast for its evoke cost, sacrifice it as it enters.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeSelfIfEvokedEffect());
    }
}
