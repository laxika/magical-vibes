package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "162")
public class SwarmingGoblins extends Card {

    public SwarmingGoblins() {
        CreateTokenEffect goblin = new CreateTokenEffect(
                1,
                "Goblin",
                1,
                1,
                CardColor.RED,
                List.of(CardSubtype.GOBLIN),
                Set.of(),
                Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RollD20Effect(
                goblin,
                goblin.withAmount(2),
                goblin.withAmount(3)));
    }
}
