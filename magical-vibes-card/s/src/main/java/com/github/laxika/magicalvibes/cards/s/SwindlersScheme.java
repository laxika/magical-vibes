package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardAndCounterTriggeringSpellIfCardTypeMatchesEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "88")
@CardRegistration(set = "NCC", collectorNumber = "96")
public class SwindlersScheme extends Card {

    public SwindlersScheme() {
        addTrigger(Triggers.opponentCastsSpell().castFrom(Zone.HAND),
                new MayEffect(new RevealTopCardAndCounterTriggeringSpellIfCardTypeMatchesEffect(), "Reveal the top card of your library?"));
    }
}
