package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardAndBoostSelfIfCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "81")
public class Bioplasm extends Card {

    public Bioplasm() {
        addTrigger(Triggers.attacks(), new ExileTopCardAndBoostSelfIfCreatureEffect());
    }
}
