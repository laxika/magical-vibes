package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "19")
public class StormHerd extends Card {

    public StormHerd() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(new ControllerLifeTotal(),
                "Pegasus", 1, 1, CardColor.WHITE, List.of(CardSubtype.PEGASUS),
                Set.of(Keyword.FLYING), Set.of()));
    }
}
