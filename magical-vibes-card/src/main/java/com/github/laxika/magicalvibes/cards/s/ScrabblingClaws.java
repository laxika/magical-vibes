package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesCardFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "237")
@CardRegistration(set = "RNA", collectorNumber = "238")
public class ScrabblingClaws extends Card {

    public ScrabblingClaws() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new TargetPlayerExilesCardFromGraveyardEffect(0)),
                "{T}: Target player exiles a card from their graveyard."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD),
                        new DrawCardEffect(1)
                ),
                "{1}, Sacrifice this artifact: Exile target card from a graveyard. Draw a card."
        ));
    }
}
