package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "14")
public class BorrowedGrace extends Card {

    public BorrowedGrace() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{1}{W}"));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +2/+0 until end of turn",
                        new BoostAllOwnCreaturesEffect(2, 0)),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +0/+2 until end of turn",
                        new BoostAllOwnCreaturesEffect(0, 2))
        )));
    }
}
