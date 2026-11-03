package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardAndCounterTriggeringSpellIfManaValueMatchesEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "31")
@CardRegistration(set = "MP2", collectorNumber = "9")
@CardRegistration(set = "MB2", collectorNumber = "157")
@CardRegistration(set = "SLD", collectorNumber = "1220")
@CardRegistration(set = "SLD", collectorNumber = "2066")
public class Counterbalance extends Card {

    public Counterbalance() {
        addTrigger(Triggers.opponentCastsSpell().anySpell(),
                new MayEffect(new RevealTopCardAndCounterTriggeringSpellIfManaValueMatchesEffect(),
                        "Reveal the top card of your library?"));
    }
}
