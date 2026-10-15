package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "160")
public class ScreamsOfTheDamned extends Card {

    public ScreamsOfTheDamned() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}",
                List.of(
                        new ExileCardFromGraveyardCost(null, false, false, false, null, null),
                        new MassDamageEffect(1, true)),
                "{1}{B}, Exile a card from your graveyard: This enchantment deals 1 damage to each creature and each player."));
    }
}
