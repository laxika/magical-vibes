package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "5ED", collectorNumber = "327")
@CardRegistration(set = "ICE", collectorNumber = "265")
@CardRegistration(set = "ME2", collectorNumber = "178")
public class Stampede extends Card {

    public Stampede() {
        addSpellEffect(new BoostAllCreaturesEffect(1, 0, new PermanentIsAttackingPredicate()));
        addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_CREATURES, new PermanentIsAttackingPredicate()));
    }
}
