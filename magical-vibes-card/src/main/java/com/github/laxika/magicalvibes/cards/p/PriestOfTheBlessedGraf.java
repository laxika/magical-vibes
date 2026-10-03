package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithMoreLandsThanController;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOC", collectorNumber = "7")
@CardRegistration(set = "VOC", collectorNumber = "45")
public class PriestOfTheBlessedGraf extends Card {

    public PriestOfTheBlessedGraf() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new CreateTokenEffect(
                        new OpponentsWithMoreLandsThanController(), "Spirit", 1, 1,
                        CardColor.WHITE, List.of(CardSubtype.SPIRIT), Set.of(), Set.of()));
    }
}
