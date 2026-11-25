package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "250")
public class VoraciousTomeSkimmer extends Card {

    public VoraciousTomeSkimmer() {
        addTrigger(Triggers.youCastSpell().duringOpponentsTurn(),
                UnlessPaysEffect.youMayPayLife(1, new DrawCardEffect(1)));
    }
}
