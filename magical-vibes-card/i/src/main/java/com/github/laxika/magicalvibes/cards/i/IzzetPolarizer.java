package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureCardFromSpellbookToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "24")
public class IzzetPolarizer extends Card {

    public IzzetPolarizer() {
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new ConjureCardFromSpellbookToHandEffect(List.of(
                        new ConjureCardFromSpellbookToHandEffect.CardPrintingReference("GRN", "179"),
                        new ConjureCardFromSpellbookToHandEffect.CardPrintingReference("GPT", "111")
                )));
    }
}
