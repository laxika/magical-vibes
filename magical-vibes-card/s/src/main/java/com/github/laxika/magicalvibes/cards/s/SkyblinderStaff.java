package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "GTC", collectorNumber = "238")
public class SkyblinderStaff extends Card {

    public SkyblinderStaff() {
        // Equipped creature gets +1/+0...
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));

        // ...and can't be blocked by creatures with flying.
        addStaticEffect(new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING)));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
