package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesOpponentPermanentToDestroyEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "DSC", collectorNumber = "24")
@CardRegistration(set = "DSC", collectorNumber = "53")
public class SadisticShellGame extends Card {

    public SadisticShellGame() {
        addSpellEffect(new EachPlayerChoosesOpponentPermanentToDestroyEffect(
                        new PermanentIsCreaturePredicate(), true, true, true));
    }
}
