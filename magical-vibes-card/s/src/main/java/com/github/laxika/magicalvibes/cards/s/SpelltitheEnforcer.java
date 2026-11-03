package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesPermanentUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "18")
public class SpelltitheEnforcer extends Card {

    public SpelltitheEnforcer() {
        addTrigger(Triggers.opponentCastsSpell().anySpell(),
                new EachOpponentSacrificesPermanentUnlessPaysEffect("{1}"));
    }
}
