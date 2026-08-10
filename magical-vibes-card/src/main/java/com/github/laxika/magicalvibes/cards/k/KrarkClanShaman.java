package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "98")
public class KrarkClanShaman extends Card {

    public KrarkClanShaman() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "Sacrifice an artifact", false),
                        new MassDamageEffect(1, false, false,
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))
                ),
                "Sacrifice an artifact: This creature deals 1 damage to each creature without flying."
        ));
    }
}
