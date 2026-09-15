package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.AdaptEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

/** Back face of Jetfire, Ingenious Scientist. */
public class JetfireAirGuardian extends Card {

    public JetfireAirGuardian() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new NotControllerTurn()),
                new GrantCardTypeEffect(CardType.CREATURE, GrantScope.SELF)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{U}{U}",
                List.of(new TransformSelfEffect(), new AdaptEffect(3)),
                "{U}{U}{U}: Convert Jetfire, then adapt 3."
        ));
    }
}
