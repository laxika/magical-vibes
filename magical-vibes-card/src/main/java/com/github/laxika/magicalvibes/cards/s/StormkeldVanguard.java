package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BearDown;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

@CardRegistration(set = "WOE", collectorNumber = "187")
public class StormkeldVanguard extends Card {

    public StormkeldVanguard() {
        setBackFaceCard(new BearDown());
        addCastingOption(new AdventureCast("{1}{G}"));
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentPowerAtMostPredicate(2)));
    }

    @Override
    public String getBackFaceClassName() {
        return "BearDown";
    }
}
