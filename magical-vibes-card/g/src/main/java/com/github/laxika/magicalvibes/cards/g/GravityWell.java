package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "ROE", collectorNumber = "185")
public class GravityWell extends Card {

    public GravityWell() {
        // Whenever a creature with flying attacks, it loses flying until end of turn.
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, new TriggeringPermanentConditionalEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING),
                new RemoveKeywordEffect(Keyword.FLYING, GrantScope.TARGET)));
    }
}
