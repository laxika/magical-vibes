package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "172")
public class ReturnOfTheWildspeaker extends Card {

    public ReturnOfTheWildspeaker() {
        var nonHuman = new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.HUMAN));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Draw cards equal to the greatest power among non-Human creatures you control",
                        new DrawCardEffect(new GreatestPowerAmongControlled(nonHuman))),
                new ChooseOneEffect.ChooseOneOption(
                        "Non-Human creatures you control get +3/+3 until end of turn",
                        new BoostAllOwnCreaturesEffect(3, 3, nonHuman))
        )));
    }
}
