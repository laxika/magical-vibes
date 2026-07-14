package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "34")
public class CreakwoodGhoul extends Card {

    public CreakwoodGhoul() {
        // {B/G}{B/G}: Exile target card from a graveyard. You gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B/G}{B/G}",
                List.of(
                        new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD),
                        new GainLifeEffect(1)
                ),
                "{B/G}{B/G}: Exile target card from a graveyard. You gain 1 life."
        ));
    }
}
