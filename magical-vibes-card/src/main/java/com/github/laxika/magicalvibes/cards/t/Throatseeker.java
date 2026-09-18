package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsUnblockedAttackingPredicate;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "110")
public class Throatseeker extends Card {

    public Throatseeker() {
        // Unblocked attacking Ninjas you control have lifelink.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.LIFELINK,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.NINJA),
                        new PermanentIsUnblockedAttackingPredicate()))));
    }
}
