package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "MBS", collectorNumber = "18")
public class VictorysHerald extends Card {

    public VictorysHerald() {
        // Whenever Victory's Herald attacks, attacking creatures gain flying and lifelink until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(
                Keyword.FLYING, GrantScope.ALL_CREATURES, new PermanentIsAttackingPredicate()
        ));
        addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(
                Keyword.LIFELINK, GrantScope.ALL_CREATURES, new PermanentIsAttackingPredicate()
        ));
    }
}
