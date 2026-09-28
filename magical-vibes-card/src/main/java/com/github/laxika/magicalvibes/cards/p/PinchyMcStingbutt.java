package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookToHandEffect;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "316")
@CardRegistration(set = "MB2", collectorNumber = "552")
public class PinchyMcStingbutt extends Card {

    public PinchyMcStingbutt() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ConjureRandomCardFromSpellbookToHandEffect(List.of(
                        new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("SOK", "64"),
                        new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("HOU", "67"),
                        new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("AKH", "189")
                )));
    }
}
