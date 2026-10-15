package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "125")
public class Downdraft extends Card {

    public Downdraft() {
        // {G}: Target creature loses flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new RemoveKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "{G}: Target creature loses flying until end of turn.",
                TargetFilters.creature()
        ));

        // Sacrifice this enchantment: It deals 2 damage to each creature with flying.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new MassDamageEffect(2, false, false, new PermanentHasKeywordPredicate(Keyword.FLYING))
                ),
                "Sacrifice this enchantment: It deals 2 damage to each creature with flying."
        ));
    }
}
