package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.ScaleDeflection;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

@CardRegistration(set = "HBG", collectorNumber = "206")
public class DreadLinnorm extends Card {

    public DreadLinnorm() {
        setBackFaceCard(new ScaleDeflection());
        addCastingOption(new AdventureCast("{3}{G}"));
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentPowerAtMostPredicate(3)));
    }

    @Override
    public String getBackFaceClassName() {
        return "ScaleDeflection";
    }
}
