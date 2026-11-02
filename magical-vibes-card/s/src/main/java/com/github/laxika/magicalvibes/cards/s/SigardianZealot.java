package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCreaturesWithDifferentPowersBoostAndGrantVigilanceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIC", collectorNumber = "29")
@CardRegistration(set = "MIC", collectorNumber = "67")
public class SigardianZealot extends Card {

    public SigardianZealot() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new ChooseCreaturesWithDifferentPowersBoostAndGrantVigilanceEffect());
    }
}
