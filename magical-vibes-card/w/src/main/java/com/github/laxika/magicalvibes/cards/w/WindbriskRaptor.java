package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "SHM", collectorNumber = "26")
public class WindbriskRaptor extends Card {

    public WindbriskRaptor() {
        // Flying auto-loads from Scryfall.
        // Attacking creatures you control have lifelink.
        addStaticEffect(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.ALL_OWN_CREATURES,
                new PermanentIsAttackingPredicate()));
    }
}
