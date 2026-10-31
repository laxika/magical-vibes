package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AFR", collectorNumber = "65")
public class MordenkainensPolymorph extends Card {

    public MordenkainensPolymorph() {
        target(TargetFilters.creature())
                .addSpellEffect(new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.DRAGON))
                .addSpellEffect(new SetBasePowerToughnessEffect(4, 4))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
    }
}
