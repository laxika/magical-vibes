package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "130")
public class IrascibleWolverine extends Card {

    public IrascibleWolverine() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTopCardMayPlayThisTurnEffect(false));

        // Plot {2}{R}
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{R}"))));
    }
}
