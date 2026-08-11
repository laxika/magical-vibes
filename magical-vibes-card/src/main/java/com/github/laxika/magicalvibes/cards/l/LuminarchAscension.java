package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerDidntLoseLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "25")
public class LuminarchAscension extends Card {

    public LuminarchAscension() {
        // At the beginning of each opponent's end step, if you didn't lose life this turn,
        // you may put a quest counter on this enchantment.
        addEffect(EffectSlot.OPPONENT_END_STEP_TRIGGERED, new ConditionalEffect(
                new ControllerDidntLoseLifeThisTurn(),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.QUEST),
                        "Put a quest counter on Luminarch Ascension?")));

        // {1}{W}: Create a 4/4 white Angel creature token with flying. Activate only if this
        // enchantment has four or more quest counters on it.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new CreateTokenEffect("Angel", 4, 4, CardColor.WHITE,
                        List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING), Set.of())),
                "{1}{W}: Create a 4/4 white Angel creature token with flying. Activate only if this enchantment has four or more quest counters on it."
        ).withRequiredSourceCounters(CounterType.QUEST, 4));
    }
}
