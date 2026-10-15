package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesMostCommonColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "128")
public class TsabosAssassin extends Card {

    public TsabosAssassin() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentSharesMostCommonColorPredicate()),
                        new DestroyTargetPermanentEffect(true))),
                "{T}: Destroy target creature if it shares a color with the most common color among all permanents or a color tied for most common. A creature destroyed this way can't be regenerated.",
                TargetFilters.creature()
        ));
    }
}
