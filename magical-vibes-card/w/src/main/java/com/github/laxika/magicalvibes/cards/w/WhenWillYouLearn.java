package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DSC", collectorNumber = "358")
public class WhenWillYouLearn extends Card {

    public WhenWillYouLearn() {
        addSpellEffect(new ExileTopCardsAndMayCastSpellsEffect(
                1, null, Subjects.eachOpponent(), false, false, null, null,
                Integer.MAX_VALUE, false, false, null, null));
    }
}
