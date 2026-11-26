package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateCost;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "14")
@CardRegistration(set = "SIR", collectorNumber = "15")
public class BorrowedGrace extends Card {

    public BorrowedGrace() {
        addCastingCost(EscalateCost.escalate("{1}{W}"));
        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +2/+0 until end of turn",
                        BoostEffect.creaturesYouControl(2, 0)),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +0/+2 until end of turn",
                        BoostEffect.creaturesYouControl(0, 2))
        )));
    }
}
