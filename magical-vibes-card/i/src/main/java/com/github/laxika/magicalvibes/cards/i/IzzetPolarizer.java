package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardFromSpellbookToHandEffect;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "24")
public class IzzetPolarizer extends Card {

    public IzzetPolarizer() {
        addEffect(EffectSlot.ON_DEATH,
                new ConjureCardFromSpellbookToHandEffect(List.of(
                        new ConjureCardFromSpellbookToHandEffect.CardPrintingReference("GRN", "179"),
                        new ConjureCardFromSpellbookToHandEffect.CardPrintingReference("GPT", "111")
                )));
    }
}
