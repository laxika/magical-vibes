package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "LTC", collectorNumber = "59")
@CardRegistration(set = "LTC", collectorNumber = "141")
@CardRegistration(set = "LTC", collectorNumber = "466")
public class LidlessGaze extends Card {

    public LidlessGaze() {
        addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(false).fromLibraryOf(Subjects.eachPlayer())
                .withAnyManaType().until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));
        addCastingOption(new FlashbackCast("{2}{B}{R}"));
    }
}
