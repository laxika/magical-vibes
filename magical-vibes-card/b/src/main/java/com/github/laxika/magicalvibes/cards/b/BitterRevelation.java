package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "KTK", collectorNumber = "65")
public class BitterRevelation extends Card {

    public BitterRevelation() {
        addSpellEffect(new LookAtTopCardsEffect(new Fixed(4), new Fixed(2), null,
                LookDestination.GRAVEYARD, false, LibrarySearchDestination.HAND, false, false,
                null, null, false, 0, true));
        addSpellEffect(new LoseLifeEffect(2));
    }
}
