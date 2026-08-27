package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "121")
public class LeapOfFlame extends Card {

    public LeapOfFlame() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{U}{R}")));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(1, 0))
                .addEffect(EffectSlot.SPELL,
                        new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE), GrantScope.TARGET));
        addEffect(EffectSlot.ON_SELF_CAST, new ReplicateEffect("{U}{R}"));
    }
}
