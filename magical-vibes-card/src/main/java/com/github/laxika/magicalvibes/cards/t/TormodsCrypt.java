package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "219")
@CardRegistration(set = "M15", collectorNumber = "237")
public class TormodsCrypt extends Card {

    public TormodsCrypt() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(),
                        new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE)),
                "{T}, Sacrifice Tormod's Crypt: Exile target player's graveyard."
        ));
    }
}
