package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "12")
public class LimitlessRekindling extends Card {

    public LimitlessRekindling() {
        addEffect(EffectSlot.SPELL,
                new ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffect(List.of(
                        new ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffect.CardPrintingReference("YECL", "3"),
                        new ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffect.CardPrintingReference("YECL", "4"),
                        new ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffect.CardPrintingReference("YECL", "11"),
                        new ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffect.CardPrintingReference("YECL", "12"),
                        new ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffect.CardPrintingReference("YECL", "18"),
                        new ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffect.CardPrintingReference("YECL", "19"),
                        new ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffect.CardPrintingReference("YECL", "33")
                )));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
