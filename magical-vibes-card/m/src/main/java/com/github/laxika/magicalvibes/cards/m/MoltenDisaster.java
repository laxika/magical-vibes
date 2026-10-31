package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToKickedSpellEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "FUT", collectorNumber = "102")
@CardRegistration(set = "MMA", collectorNumber = "123")
@CardRegistration(set = "C13", collectorNumber = "117")
public class MoltenDisaster extends Card {

    public MoltenDisaster() {
        addStaticEffect(new KickerEffect("{R}"));
        addStaticEffect(new GrantKeywordsToKickedSpellEffect(Keyword.SPLIT_SECOND));
        addSpellEffect(new MassDamageEffect(new XValue(), true, false,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
    }
}
