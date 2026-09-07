package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PettyTheft;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "ELD", collectorNumber = "39")
public class BrazenBorrower extends Card {

    public BrazenBorrower() {
        setBackFaceCard(new PettyTheft());
        addCastingOption(new AdventureCast("{1}{U}"));
        addEffect(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING),
                "creatures with flying"
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "PettyTheft";
    }
}
