package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;

@CardRegistration(set = "YBRO", collectorNumber = "21")
public class PerilousIteration extends Card {

    public PerilousIteration() {
        addSpellEffect(SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect.nextTurnsEndStep(
                        new CardMaxManaValuePredicate(2)));
        addSpellEffect(SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect.nextTurnsEndStep(
                        new CardMinManaValuePredicate(3)));
    }
}
