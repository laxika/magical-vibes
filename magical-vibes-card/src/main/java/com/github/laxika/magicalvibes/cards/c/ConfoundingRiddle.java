package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "50")
public class ConfoundingRiddle extends Card {

    public ConfoundingRiddle() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Look at the top four cards of your library. Put one of them into your hand and the rest into your graveyard",
                        LookAtTopCardsEffect.chooseNToHandRestToGraveyard(4, 1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell unless its controller pays {4}",
                        new CounterUnlessPaysEffect(4))
        )));
    }
}
