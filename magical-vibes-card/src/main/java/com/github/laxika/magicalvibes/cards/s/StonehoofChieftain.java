package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "325")
@CardRegistration(set = "CMM", collectorNumber = "573")
public class StonehoofChieftain extends Card {

    public StonehoofChieftain() {
        // Whenever another creature you control attacks, it gains trample and indestructible until
        // end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                        new GrantKeywordEffect(
                                Set.of(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE), GrantScope.TARGET)));
    }
}
