package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "99")
@CardRegistration(set = "KLR", collectorNumber = "105")
public class RushOfVitality extends Card {

    public RushOfVitality() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(1, 0))
                .addSpellEffect(new GrantKeywordEffect(
                        Set.of(Keyword.LIFELINK, Keyword.INDESTRUCTIBLE), GrantScope.TARGET));
    }
}
