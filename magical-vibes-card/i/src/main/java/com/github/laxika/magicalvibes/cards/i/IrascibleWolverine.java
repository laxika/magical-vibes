package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "130")
public class IrascibleWolverine extends Card {

    public IrascibleWolverine() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileTopCardMayPlayThisTurnEffect(false));

        // Plot {2}{R}
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{R}"))));
    }
}
