package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExchangeLifeTotalWithToughnessEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "207")
public class TreeOfRedemption extends Card {

    public TreeOfRedemption() {
        // {T}: Exchange your life total with Tree of Redemption's toughness.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new ExchangeLifeTotalWithToughnessEffect()),
                "{T}: Exchange your life total with Tree of Redemption's toughness."));
    }
}
