package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "121")
public class DraconauticsEngineer extends Card {

    public DraconauticsEngineer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES,
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "Exhaust — {R}: Other creatures you control gain haste until end of turn. Put a +1/+1 counter on this creature."
                        + " (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new CreateTokenEffect(
                        "Dinosaur Dragon", 4, 4, CardColor.RED,
                        List.of(CardSubtype.DINOSAUR, CardSubtype.DRAGON),
                        Set.of(Keyword.FLYING), Set.of())),
                "Exhaust — {3}{R}: Create a 4/4 red Dinosaur Dragon creature token with flying."
                        + " (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
