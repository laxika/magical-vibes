package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachLibraryAndGrantLesserManaValueFreePlayEffect;

@CardRegistration(set = "FIN", collectorNumber = "166")
@CardRegistration(set = "FIN", collectorNumber = "340")
public class TripleTriad extends Card {

    public TripleTriad() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ExileTopCardOfEachLibraryAndGrantLesserManaValueFreePlayEffect());
    }
}
