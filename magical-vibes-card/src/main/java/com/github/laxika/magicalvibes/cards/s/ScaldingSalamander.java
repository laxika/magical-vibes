package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDefendingPlayerCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "EXO", collectorNumber = "100")
public class ScaldingSalamander extends Card {

    public ScaldingSalamander() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new DealDamageToDefendingPlayerCreaturesEffect(1,
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))),
                "Have Scalding Salamander deal 1 damage to each creature without flying defending player controls?"));
    }
}
