package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;

@CardRegistration(set = "AFC", collectorNumber = "47")
public class FeveredSuspicion extends Card {

    public FeveredSuspicion() {
        addSpellEffect(new EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect(
                        Integer.MAX_VALUE, false, LibraryScope.EACH_OPPONENT));
    }
}
