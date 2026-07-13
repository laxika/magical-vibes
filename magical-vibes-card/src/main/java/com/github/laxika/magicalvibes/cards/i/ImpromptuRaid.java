package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldElseGraveyardEffect;
import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "209")
public class ImpromptuRaid extends Card {

    public ImpromptuRaid() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R/G}",
                List.of(new RevealTopCardCreatureToBattlefieldElseGraveyardEffect(true, true)),
                "{2}{R/G}: Reveal the top card of your library. If it isn't a creature card, put it "
                        + "into your graveyard. Otherwise, put that card onto the battlefield. That creature "
                        + "gains haste. Sacrifice it at the beginning of the next end step."));
    }
}
