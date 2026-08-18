package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOK", collectorNumber = "146")
public class SeedTheLand extends Card {

    public SeedTheLand() {
        CreateTokenEffect snake = new CreateTokenEffect(
                "Snake", 1, 1, CardColor.GREEN, List.of(CardSubtype.SNAKE), Set.of(), Set.of());
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, snake);
        addEffect(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD,
                new CreateTokenForTargetPlayerEffect(snake));
    }
}
