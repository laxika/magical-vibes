package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "144")
public class MarshLurker extends Card {

    public MarshLurker() {
        // Sacrifice a Swamp: This creature gains fear until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.SWAMP), "Sacrifice a Swamp", false),
                        new GrantKeywordEffect(Keyword.FEAR, GrantScope.SELF)
                ),
                "Sacrifice a Swamp: This creature gains fear until end of turn."
        ));
    }
}
