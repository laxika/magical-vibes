package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "TDM", collectorNumber = "214")
public class RakshasasBargain extends Card {

    public RakshasasBargain() {
        addSpellEffect(LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(4, 2));
    }
}
