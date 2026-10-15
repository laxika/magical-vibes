package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesSubtypeUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "304")
public class UrsineChampion extends Card {

    public UrsineChampion() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}",
                List.of(
                        new BoostSelfEffect(3, 3),
                        new SourceBecomesSubtypeUntilEndOfTurnEffect(List.of(CardSubtype.BEAR, CardSubtype.BERSERKER))
                ),
                "{5}{G}: This creature gets +3/+3 and becomes a Bear Berserker until end of turn. Activate only once each turn.",
                1
        ));
    }
}
