package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTargetPlayerTopCardMayGraveyardEffect;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "317")
public class WandOfDenial extends Card {

    public WandOfDenial() {
        // {T}: Look at the top card of target player's library. If it's a nonland card,
        // you may pay 2 life. If you do, put it into that player's graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new LookAtTargetPlayerTopCardMayGraveyardEffect(true, 2)),
                "{T}: Look at the top card of target player's library. If it's a nonland card, "
                        + "you may pay 2 life. If you do, put it into that player's graveyard."
        ));
    }
}
