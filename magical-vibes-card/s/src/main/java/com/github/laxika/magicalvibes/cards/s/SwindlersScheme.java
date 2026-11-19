package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardAndCounterTriggeringSpellIfMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.RevealedCardMatch;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "88")
@CardRegistration(set = "NCC", collectorNumber = "96")
public class SwindlersScheme extends Card {

    public SwindlersScheme() {
        addTrigger(Triggers.opponentCastsSpell().castFrom(Zone.HAND),
                new MayEffect(new RevealTopCardAndCounterTriggeringSpellIfMatchesEffect(
                        RevealedCardMatch.SHARES_CARD_TYPE, true), "Reveal the top card of your library?"));
    }
}
