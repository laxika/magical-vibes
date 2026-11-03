package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "2344")
@CardRegistration(set = "SLD", collectorNumber = "2434")
@CardRegistration(set = "C19", collectorNumber = "6")
public class SongOfTheWorldsoul extends Card {

    public SongOfTheWorldsoul() {
        addTrigger(Triggers.youCastSpell().anySpell(), new PopulateEffect());
    }
}
