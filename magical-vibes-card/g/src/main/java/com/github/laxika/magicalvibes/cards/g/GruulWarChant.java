package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "DGM", collectorNumber = "75")
@CardRegistration(set = "MM3", collectorNumber = "170")
public class GruulWarChant extends Card {

    public GruulWarChant() {
        // Attacking creatures you control get +1/+0 and have menace.
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.ALL_OWN_CREATURES, new PermanentIsAttackingPredicate()));
        addStaticEffect(new GrantKeywordEffect(
                Keyword.MENACE, GrantScope.ALL_OWN_CREATURES, new PermanentIsAttackingPredicate()));
    }
}
