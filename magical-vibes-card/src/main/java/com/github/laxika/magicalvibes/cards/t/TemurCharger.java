package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "153")
public class TemurCharger extends Card {

    public TemurCharger() {
        addMorphWithRevealCost(new CardColorPredicate(CardColor.GREEN), "green");
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_TURNED_FACE_UP,
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
    }
}
