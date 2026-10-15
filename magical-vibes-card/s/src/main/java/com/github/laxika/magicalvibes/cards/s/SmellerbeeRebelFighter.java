package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "TLE", collectorNumber = "125")
@CardRegistration(set = "TLE", collectorNumber = "198")
public class SmellerbeeRebelFighter extends Card {

    public SmellerbeeRebelFighter() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new DiscardOwnHandThenDrawEffect(
                        new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER)),
                "Discard your hand?"));
    }
}
