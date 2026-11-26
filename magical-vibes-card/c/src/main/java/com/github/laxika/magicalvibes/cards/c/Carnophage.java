package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "EXO", collectorNumber = "53")
@CardRegistration(set = "TPR", collectorNumber = "84")
@CardRegistration(set = "VMA", collectorNumber = "107")
public class Carnophage extends Card {

    public Carnophage() {
        // At the beginning of your upkeep, tap this creature unless you pay 1 life.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPayLife(1, new TapPermanentsEffect(Subjects.source())));
    }
}
