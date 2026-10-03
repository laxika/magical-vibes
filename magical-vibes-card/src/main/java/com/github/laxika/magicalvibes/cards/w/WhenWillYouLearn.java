package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;

@CardRegistration(set = "DSC", collectorNumber = "358")
public class WhenWillYouLearn extends Card {

    public WhenWillYouLearn() {
        addEffect(EffectSlot.SPELL, new ExileTopCardsAndMayCastSpellsEffect(
                1, null, LibraryScope.EACH_OPPONENT, false, false, null, null,
                Integer.MAX_VALUE, false, false, false, null, null));
    }
}
