package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "30")
public class SanctifiedCharge extends Card {

    public SanctifiedCharge() {
        addSpellEffect(new BoostAllOwnCreaturesEffect(2, 1));
        addSpellEffect(new GrantKeywordEffect(
                Keyword.FIRST_STRIKE,
                GrantScope.OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))));
    }
}
