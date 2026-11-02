package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetCreatureWhenSingleTargetSpellCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "512")
public class LokiGodOfLies extends Card {

    public LokiGodOfLies() {
        addTrigger(Triggers.youCastSpell(),
                new GainControlOfTargetCreatureWhenSingleTargetSpellCastEffect());
    }
}
