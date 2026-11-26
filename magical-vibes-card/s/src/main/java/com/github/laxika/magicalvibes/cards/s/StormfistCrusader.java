package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "203")
@CardRegistration(set = "ELD", collectorNumber = "383")
@CardRegistration(set = "DSC", collectorNumber = "234")
@CardRegistration(set = "SCD", collectorNumber = "247")
public class StormfistCrusader extends Card {

    public StormfistCrusader() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                SequenceEffect.of(
                        new DrawCardEffect(1, Subjects.eachPlayer()),
                        new LoseLifeEffect(1, Subjects.eachPlayer())));
    }
}
