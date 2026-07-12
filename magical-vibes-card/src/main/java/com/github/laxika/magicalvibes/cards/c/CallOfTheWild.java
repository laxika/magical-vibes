package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldElseGraveyardEffect;
import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "235")
public class CallOfTheWild extends Card {

    public CallOfTheWild() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{G}",
                List.of(new RevealTopCardCreatureToBattlefieldElseGraveyardEffect()),
                "{2}{G}{G}: Reveal the top card of your library. If it's a creature card, put it onto "
                        + "the battlefield. Otherwise, put it into your graveyard."));
    }
}
