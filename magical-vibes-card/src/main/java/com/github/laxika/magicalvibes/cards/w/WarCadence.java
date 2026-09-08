package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToBlockThisTurnEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "224")
public class WarCadence extends Card {

    public WarCadence() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{R}",
                List.of(new RequirePaymentToBlockThisTurnEffect(new XValue())),
                "{X}{R}: This turn, creatures can't block unless their controller pays {X} for each blocking creature they control."));
    }
}
