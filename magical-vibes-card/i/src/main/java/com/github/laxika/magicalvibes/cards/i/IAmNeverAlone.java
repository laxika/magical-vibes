package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCommanderEffect;

@CardRegistration(set = "DSC", collectorNumber = "335")
public class IAmNeverAlone extends Card {

    public IAmNeverAlone() {
        addSpellEffect(new CreateTokenCopyOfCommanderEffect());
    }
}
