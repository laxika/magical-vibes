package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "101")
public class SeethingPathblazer extends Card {

    public SeethingPathblazer() {
        // Sacrifice an Elemental: This creature gets +2/+0 and gains first strike until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.ELEMENTAL),
                                "Sacrifice an Elemental", false),
                        new BoostSelfEffect(2, 0),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "Sacrifice an Elemental: This creature gets +2/+0 and gains first strike until end of turn."));
    }
}
