package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;

@CardRegistration(set = "RNA", collectorNumber = "118")
public class SpikewheelAcrobat extends Card {

    public SpikewheelAcrobat() {
        addCastingOption(AlternateHandCast.spectacle("{2}{R}", new OpponentLostLifeThisTurn(1)));
    }
}
