package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardAndCounterTriggeringSpellIfCardTypeMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastFromHandTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "88")
@CardRegistration(set = "NCC", collectorNumber = "96")
public class SwindlersScheme extends Card {

    public SwindlersScheme() {
        addTrigger(Triggers.opponentCastsSpell(), new MayEffect(
                new SpellCastFromHandTriggerEffect(null,
                        List.of(new RevealTopCardAndCounterTriggeringSpellIfCardTypeMatchesEffect())),
                "Reveal the top card of your library?"));
    }
}
