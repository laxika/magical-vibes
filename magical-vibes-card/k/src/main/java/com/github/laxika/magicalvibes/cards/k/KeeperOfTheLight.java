package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerHasMoreLifeThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "8")
public class KeeperOfTheLight extends Card {

    public KeeperOfTheLight() {
        // {W}, {T}: Choose target opponent who has more life than you do as you activate this
        // ability. You gain 3 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new GainLifeEffect(new Fixed(3), GainLifeRecipient.CONTROLLER, true)),
                "{W}, {T}: Choose target opponent who has more life than you do as you activate this ability. "
                        + "You gain 3 life.",
                new PlayerPredicateTargetFilter(
                        new PlayerHasMoreLifeThanControllerPredicate(),
                        "Target opponent must have more life than you"
                )
        ));
    }
}
