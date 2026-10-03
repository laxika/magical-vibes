package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayersDiscardHandsThenOpponentsDrawEffect;

@CardRegistration(set = "DSC", collectorNumber = "367")
public class YourPlansMeanNothing extends Card {

    public YourPlansMeanNothing() {
        target(0, 99).addEffect(EffectSlot.SPELL,
                new TargetPlayersDiscardHandsThenOpponentsDrawEffect());
    }
}
