package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "232")
public class SyrVondamTheLucent extends Card {

    public SyrVondamTheLucent() {
        var otherCreatures = new PermanentNotPredicate(new PermanentIsSourceCardPredicate());
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new BoostAllOwnCreaturesEffect(1, 0, otherCreatures),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.OWN_CREATURES, otherCreatures));
        addTrigger(Triggers.attacks(),
                new BoostAllOwnCreaturesEffect(1, 0, otherCreatures),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.OWN_CREATURES, otherCreatures));
    }
}
