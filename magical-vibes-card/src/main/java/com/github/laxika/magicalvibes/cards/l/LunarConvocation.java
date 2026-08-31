package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControllerDidntLoseLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "223")
public class LunarConvocation extends Card {

    public LunarConvocation() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT)));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new AllConditions(List.of(
                        new GainedLifeThisTurn(),
                        new NotCondition(new ControllerDidntLoseLifeThisTurn())
                )),
                new CreateTokenEffect("Bat", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.BAT), Set.of(Keyword.FLYING), Set.of())));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new PayLifeCost(2), new DrawCardEffect(1)),
                "{1}{B}, Pay 2 life: Draw a card."
        ));
    }
}
