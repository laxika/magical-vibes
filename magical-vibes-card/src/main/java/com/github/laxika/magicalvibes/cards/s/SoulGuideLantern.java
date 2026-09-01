package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "251")
@CardRegistration(set = "THB", collectorNumber = "237")
public class SoulGuideLantern extends Card {

    public SoulGuideLantern() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_OPPONENTS)
                ),
                "{T}, Sacrifice this artifact: Exile each opponent's graveyard."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{1}, {T}, Sacrifice this artifact: Draw a card."
        ));
    }
}
