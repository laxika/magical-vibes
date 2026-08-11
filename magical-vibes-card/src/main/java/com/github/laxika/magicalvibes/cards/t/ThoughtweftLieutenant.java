package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "246")
public class ThoughtweftLieutenant extends Card {

    public ThoughtweftLieutenant() {
        // Whenever this creature or another Kithkin you control enters, target creature you control
        // gets +1/+1 and gains trample until end of turn.
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                        new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.KITHKIN),
                                new BoostTargetCreatureEffect(1, 1)))
                .addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                        new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.KITHKIN),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)));
    }
}
