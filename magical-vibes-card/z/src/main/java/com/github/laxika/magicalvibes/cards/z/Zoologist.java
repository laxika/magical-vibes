package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldElseGraveyardEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "285")
public class Zoologist extends Card {

    public Zoologist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{G}",
                List.of(new RevealTopCardCreatureToBattlefieldElseGraveyardEffect()),
                "{3}{G}, {T}: Reveal the top card of your library. If it's a creature card, put it onto "
                        + "the battlefield. Otherwise, put it into your graveyard."));
    }
}
