package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "176")
public class KorozdaGuildmage extends Card {

    public KorozdaGuildmage() {
        // {1}{B}{G}: Target creature gets +1/+1 and gains intimidate until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{G}",
                List.of(
                        new BoostTargetCreatureEffect(1, 1),
                        new GrantKeywordEffect(Keyword.INTIMIDATE, GrantScope.TARGET)
                ),
                "{1}{B}{G}: Target creature gets +1/+1 and gains intimidate until end of turn.",
                TargetFilters.creature()
        ));

        // {2}{B}{G}, Sacrifice a nontoken creature: Create X 1/1 green Saproling creature tokens,
        // where X is the sacrificed creature's toughness.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}{G}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentIsTokenPredicate())
                                )),
                                "a nontoken creature",
                                false,
                                false,
                                false,
                                true),
                        new CreateTokenEffect(new XValue(), "Saproling", 1, 1,
                                CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of())
                ),
                "{2}{B}{G}, Sacrifice a nontoken creature: Create X 1/1 green Saproling creature tokens, "
                        + "where X is the sacrificed creature's toughness."
        ));
    }
}
