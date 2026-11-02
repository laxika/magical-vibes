package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "316")
@CardRegistration(set = "MB2", collectorNumber = "552")
public class PinchyMcStingbutt extends Card {

    public PinchyMcStingbutt() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ConjureRandomCardFromSpellbookToHandEffect(List.of(
                        new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("SOK", "64"),
                        new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("HOU", "67"),
                        new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("AKH", "189")
                )));
    }
}
