package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChosenNameSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfEachLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "53")
public class MedomaisProphecy extends Card {

    public MedomaisProphecy() {
        addTrigger(Triggers.chapter(1), new ScryEffect(2));
        addTrigger(Triggers.chapter(2), new ChooseCardNameAtResolutionEffect());
        addTrigger(Triggers.chapter(3),
                new ChosenNameSpellCastTriggerEffect(List.of(new DrawCardEffect(2))));
        addTrigger(Triggers.chapter(4), new LookAtTopCardOfEachLibraryEffect());
    }
}
