package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.WonClash;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "LRW", collectorNumber = "212")
public class FistfulOfForce extends Card {

    public FistfulOfForce() {
        // Target creature gets +2/+2 until end of turn. Clash with an opponent. If you win, that
        // creature gets an additional +2/+2 and gains trample until end of turn. All effects share
        // the single creature target group; the win-only boost/keyword are gated on WonClash
        // (mirrors Lash Out's base-effect + ClashEffect(null) + ConditionalEffect(WonClash) shape).
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"))
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2))
                .addEffect(EffectSlot.SPELL, new ClashEffect(null))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new WonClash(),
                        new BoostTargetCreatureEffect(2, 2)))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new WonClash(),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)));
    }
}
