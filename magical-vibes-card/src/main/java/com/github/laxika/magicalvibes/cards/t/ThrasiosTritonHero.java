package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardLandToTappedBattlefieldElseDrawEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "SPG", collectorNumber = "16")
public class ThrasiosTritonHero extends Card {

    public ThrasiosTritonHero() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new ScryEffect(1), new RevealTopCardLandToTappedBattlefieldElseDrawEffect()),
                "{4}: Scry 1, then reveal the top card of your library. If it's a land card, put it "
                        + "onto the battlefield tapped. Otherwise, draw a card."
        ));
    }
}
