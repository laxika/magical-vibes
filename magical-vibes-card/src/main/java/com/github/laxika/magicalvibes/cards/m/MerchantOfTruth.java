package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MKC", collectorNumber = "11")
@CardRegistration(set = "MKC", collectorNumber = "322")
@CardRegistration(set = "FDC", collectorNumber = "39")
public class MerchantOfTruth extends Card {

    public MerchantOfTruth() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, CreateTokenEffect.ofClueToken(1));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.EXALTED,
                GrantScope.OWN_PERMANENTS,
                new PermanentHasSubtypePredicate(CardSubtype.CLUE)));
    }
}
