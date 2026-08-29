package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentCountAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "22")
public class ShelteringPrayers extends Card {

    public ShelteringPrayers() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.SHROUD,
                GrantScope.ALL_LANDS,
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSupertypePredicate(CardSupertype.BASIC),
                        new PermanentControllerControlsPermanentCountAtMostPredicate(
                                3, new PermanentIsLandPredicate())))));
    }
}
