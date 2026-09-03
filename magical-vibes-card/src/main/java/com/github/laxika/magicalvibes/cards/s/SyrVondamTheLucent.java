package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "EOE", collectorNumber = "232")
public class SyrVondamTheLucent extends Card {

    public SyrVondamTheLucent() {
        var otherCreatures = new PermanentNotPredicate(new PermanentIsSourceCardPredicate());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostAllOwnCreaturesEffect(1, 0, otherCreatures));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.OWN_CREATURES, otherCreatures));
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(1, 0, otherCreatures));
        addEffect(EffectSlot.ON_ATTACK,
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.OWN_CREATURES, otherCreatures));
    }
}
