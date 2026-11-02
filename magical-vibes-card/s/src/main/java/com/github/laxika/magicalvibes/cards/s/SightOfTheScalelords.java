package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "207")
public class SightOfTheScalelords extends Card {

    public SightOfTheScalelords() {
        PermanentToughnessAtLeastPredicate toughnessAtLeastFour = new PermanentToughnessAtLeastPredicate(4);
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new BoostAllOwnCreaturesEffect(2, 2, toughnessAtLeastFour));
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES, toughnessAtLeastFour));
    }
}
