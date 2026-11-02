package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "117")
public class InkTreaderNephilim extends Card {

    public InkTreaderNephilim() {
        addTrigger(Triggers.playerCastsSpell(),
                new CopySpellForEachOtherCreatureEffect());
    }
}
