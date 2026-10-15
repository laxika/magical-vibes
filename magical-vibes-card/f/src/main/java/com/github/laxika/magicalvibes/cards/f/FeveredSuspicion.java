package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;

@CardRegistration(set = "AFC", collectorNumber = "47")
public class FeveredSuspicion extends Card {

    public FeveredSuspicion() {
        addEffect(EffectSlot.SPELL,
                new EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect(
                        Integer.MAX_VALUE, false, LibraryScope.EACH_OPPONENT));
    }
}
