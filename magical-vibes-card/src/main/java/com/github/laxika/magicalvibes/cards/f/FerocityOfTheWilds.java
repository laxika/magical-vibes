package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "123")
public class FerocityOfTheWilds extends Card {

    public FerocityOfTheWilds() {
        var attackingNonHumans = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.HUMAN))
        ));
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES, attackingNonHumans));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES, attackingNonHumans));
    }
}
