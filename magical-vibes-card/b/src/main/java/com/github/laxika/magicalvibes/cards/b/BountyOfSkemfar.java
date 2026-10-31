package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsRevealTwoTypesToHandThenRestEffect;

@CardRegistration(set = "KHC", collectorNumber = "12")
public class BountyOfSkemfar extends Card {

    public BountyOfSkemfar() {
        addSpellEffect(LookAtTopCardsRevealTwoTypesToHandThenRestEffect
                        .landAndSubtypeToBattlefieldAndHandRestOnBottomRandom(6, CardSubtype.ELF));
    }
}
