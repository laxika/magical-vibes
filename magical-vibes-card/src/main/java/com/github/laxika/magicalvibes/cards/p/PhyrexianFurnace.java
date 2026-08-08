package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileBottomCardOfTargetPlayerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "155")
public class PhyrexianFurnace extends Card {

    public PhyrexianFurnace() {
        // {T}: Exile the bottom card of target player's graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileBottomCardOfTargetPlayerGraveyardEffect()),
                "{T}: Exile the bottom card of target player's graveyard."
        ));

        // {1}, Sacrifice this artifact: Exile target card from a graveyard. Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD),
                        new DrawCardEffect()
                ),
                "{1}, Sacrifice Phyrexian Furnace: Exile target card from a graveyard. Draw a card."
        ));
    }
}
