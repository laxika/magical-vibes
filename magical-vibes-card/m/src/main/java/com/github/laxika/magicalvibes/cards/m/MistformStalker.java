package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesChosenSubtypeUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "98")
public class MistformStalker extends Card {

    public MistformStalker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SourceBecomesChosenSubtypeUntilEndOfTurnEffect()),
                "{1}: This creature becomes the creature type of your choice until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}{U}",
                List.of(
                        new BoostSelfEffect(2, 2),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)
                ),
                "{2}{U}{U}: This creature gets +2/+2 and gains flying until end of turn."
        ));
    }
}
