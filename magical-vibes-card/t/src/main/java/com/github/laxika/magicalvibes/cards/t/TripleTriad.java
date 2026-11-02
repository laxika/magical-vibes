package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachLibraryAndGrantLesserManaValueFreePlayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "166")
@CardRegistration(set = "FIN", collectorNumber = "340")
public class TripleTriad extends Card {

    public TripleTriad() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ExileTopCardOfEachLibraryAndGrantLesserManaValueFreePlayEffect());
    }
}
