package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "239")
public class TormodsCryptkeeper extends Card {

    public TormodsCryptkeeper() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(),
                        new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE)),
                "{T}, Sacrifice this creature: Exile target player's graveyard."
        ));
    }
}
