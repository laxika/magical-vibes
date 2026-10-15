package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesSubtypeUntilEndOfTurnEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "120")
public class BogardanDragonheart extends Card {

    public BogardanDragonheart() {
        // Sacrifice another creature: Until end of turn, this creature becomes a Dragon with
        // base power and toughness 4/4, flying, and haste.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new SourceBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.DRAGON),
                        new SetBasePowerToughnessEffect(4, 4, GrantScope.SELF),
                        new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.HASTE), GrantScope.SELF)
                ),
                "Sacrifice another creature: Until end of turn, this creature becomes a Dragon "
                        + "with base power and toughness 4/4, flying, and haste."
        ));
    }
}
