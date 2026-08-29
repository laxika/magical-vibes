package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackThisTurnEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "113")
public class WarTax extends Card {

    public WarTax() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{U}",
                List.of(new RequirePaymentToAttackThisTurnEffect(new XValue())),
                "{X}{U}: This turn, creatures can't attack unless their controller pays {X} for each attacking creature they control."));
    }
}
