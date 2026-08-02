package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "97")
public class LegionLoyalist extends Card {

    public LegionLoyalist() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new MinimumAttackers(3),
                SequenceEffect.of(
                        new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.TRAMPLE), GrantScope.OWN_CREATURES),
                        new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.TRAMPLE), GrantScope.SELF),
                        new GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect(
                                null,
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                                "nontoken creatures"))));
    }
}
