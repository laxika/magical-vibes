package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardLandToBattlefieldElseToHandEffect;
import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "125")
public class SkywardEyeProphets extends Card {

    public SkywardEyeProphets() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new RevealTopCardLandToBattlefieldElseToHandEffect()),
                "{T}: Reveal the top card of your library. If it's a land card, put it onto the "
                        + "battlefield. Otherwise, put it into your hand."));
    }
}
