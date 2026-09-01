package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerMayDrawUpToNCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "119")
public class ShahOfNaarIsle extends Card {

    public ShahOfNaarIsle() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RegisterEchoAtNextUpkeepEffect("{0}",
                        List.of(new EachOtherPlayerMayDrawUpToNCardsEffect(3))));
    }
}
