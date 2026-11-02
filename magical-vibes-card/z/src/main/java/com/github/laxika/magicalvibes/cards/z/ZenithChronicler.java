package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardsForEachOpponentOfTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.FirstMulticoloredSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "246")
public class ZenithChronicler extends Card {

    public ZenithChronicler() {
        addTrigger(Triggers.playerCastsSpell(),
                new FirstMulticoloredSpellCastTriggerEffect(
                        List.of(new DrawCardsForEachOpponentOfTriggeringPlayerEffect(1))));
    }
}
