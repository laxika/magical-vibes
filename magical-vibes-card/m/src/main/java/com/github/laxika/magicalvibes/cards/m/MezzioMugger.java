package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "49")
@CardRegistration(set = "NCC", collectorNumber = "149")
public class MezzioMugger extends Card {

    public MezzioMugger() {
        addCastingOption(AlternateHandCast.blitz("{2}{R}"));

        addTrigger(Triggers.attacks(),
                new ExileTopCardMayPlayThisTurnEffect(false).fromLibraryOf(Subjects.eachPlayer()).withManaAsThoughAnyColor());
    }
}
