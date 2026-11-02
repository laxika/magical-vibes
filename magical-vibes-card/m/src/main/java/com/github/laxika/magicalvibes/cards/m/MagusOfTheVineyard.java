package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaToActivePlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "132")
@CardRegistration(set = "CMD", collectorNumber = "166")
public class MagusOfTheVineyard extends Card {

    public MagusOfTheVineyard() {
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.ANY),
                new AwardManaToActivePlayerEffect(ManaColor.GREEN, 2));
    }
}
