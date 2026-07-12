package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "66")
public class CoastalHornclaw extends Card {

    public CoastalHornclaw() {
        // Sacrifice a land: This creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land"),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "Sacrifice a land: This creature gains flying until end of turn."));
    }
}
