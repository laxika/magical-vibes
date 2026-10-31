package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileHandThenDrawAndMayPlayUntilNextTurnEffect;

@CardRegistration(set = "MSH", collectorNumber = "133")
public class HexMagic extends Card {

    public HexMagic() {
        addSpellEffect(new ExileHandThenDrawAndMayPlayUntilNextTurnEffect());
    }
}
