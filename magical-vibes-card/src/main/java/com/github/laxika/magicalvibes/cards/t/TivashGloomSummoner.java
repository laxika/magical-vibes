package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeAndCreateTokenEqualToAmountEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C21", collectorNumber = "45")
public class TivashGloomSummoner extends Card {

    public TivashGloomSummoner() {
        // At the beginning of your end step, if you gained life this turn, you may pay X life,
        // where X is the amount of life you gained this turn. If you do, create an X/X black Demon
        // creature token with flying.
        LifeGainedThisTurn lifeGained = new LifeGainedThisTurn(CountScope.CONTROLLER);
        CreateTokenEffect demon = new CreateTokenEffect(
                "Demon", lifeGained, lifeGained, CardColor.BLACK,
                List.of(CardSubtype.DEMON), Set.of(Keyword.FLYING), Set.of());
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(),
                new MayPayLifeAndCreateTokenEqualToAmountEffect(lifeGained, demon)));
    }
}
