package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "56")
public class CobbledLancer extends Card {

    public CobbledLancer() {
        // As an additional cost to cast this spell, exile a creature card from your graveyard.
        addEffect(EffectSlot.SPELL, new ExileCardFromGraveyardCost(CardType.CREATURE));

        // {3}{U}, Exile this card from your graveyard: Draw a card.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new DrawCardEffect(1)
                ),
                "{3}{U}, Exile this card from your graveyard: Draw a card."
        ));
    }
}
