package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokensPerControlledCreatureSubtypeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "99")
public class EndlessRanksOfTheDead extends Card {

    public EndlessRanksOfTheDead() {
        // At the beginning of your upkeep, create X 2/2 black Zombie creature tokens,
        // where X is half the number of Zombies you control, rounded down.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokensPerControlledCreatureSubtypeEffect(
                CardSubtype.ZOMBIE, 2, "Zombie", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of()
        ));
    }
}
