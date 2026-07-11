package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "58")
public class Bitterblossom extends Card {

    public Bitterblossom() {
        // At the beginning of your upkeep, you lose 1 life and create a 1/1 black Faerie Rogue creature token with flying.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new LoseLifeEffect(1));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokenEffect(
                "Faerie Rogue", 1, 1, CardColor.BLACK,
                List.of(CardSubtype.FAERIE, CardSubtype.ROGUE),
                Set.of(Keyword.FLYING), Set.of()));
    }
}
