package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTargetPlayerTopCardMayGraveyardEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "249")
public class LurkingInformant extends Card {

    public LurkingInformant() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new LookAtTargetPlayerTopCardMayGraveyardEffect()),
                "{2}, {T}: Look at the top card of target player's library. You may put that card into that player's graveyard."
        ));
    }
}
