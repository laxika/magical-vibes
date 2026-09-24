package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "2031")
@CardRegistration(set = "SLD", collectorNumber = "2032")
public class ScionOfDraco extends Card {

    public ScionOfDraco() {
        addEffect(EffectSlot.STATIC,
                new ReduceOwnCastCostEffect(new Scaled(new BasicLandTypesAmongControlledLands(), 2)));

        addEffect(EffectSlot.STATIC, keywordGrant(Keyword.VIGILANCE, CardColor.WHITE));
        addEffect(EffectSlot.STATIC, keywordGrant(Keyword.HEXPROOF, CardColor.BLUE));
        addEffect(EffectSlot.STATIC, keywordGrant(Keyword.LIFELINK, CardColor.BLACK));
        addEffect(EffectSlot.STATIC, keywordGrant(Keyword.FIRST_STRIKE, CardColor.RED));
        addEffect(EffectSlot.STATIC, keywordGrant(Keyword.TRAMPLE, CardColor.GREEN));
    }

    private GrantKeywordEffect keywordGrant(Keyword keyword, CardColor color) {
        return new GrantKeywordEffect(keyword, GrantScope.ALL_OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(color)));
    }
}
