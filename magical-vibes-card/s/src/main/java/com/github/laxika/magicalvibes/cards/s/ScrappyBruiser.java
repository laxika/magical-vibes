package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "162")
@CardRegistration(set = "MOM", collectorNumber = "382")
public class ScrappyBruiser extends Card {

    public ScrappyBruiser() {
        target(TargetFilters.attackingCreature(), 0, 1)
                .addTrigger(Triggers.attacks(),
                        new BoostTargetCreatureEffect(2, 0),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET),
                        new ReturnTargetPermanentToHandAtEndOfCombatEffect());
    }
}
