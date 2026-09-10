package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "259")
public class LanternOfTheLost extends Card {

    public LanternOfTheLost() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new ExileSelfCost(),
                        new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS),
                        new DrawCardEffect()
                ),
                "{1}, {T}, Exile this artifact: Exile all cards from all graveyards, then draw a card."
        ));
    }
}
