package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "22")
public class NestingBot extends Card {

    public NestingBot() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                "Servo", 1, 1, null, List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new MaxSpeed(),
                new StaticBoostEffect(1, 0, GrantScope.SELF)));
    }
}
