package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "SOC", collectorNumber = "264")
public class BlossomingBogbeast extends Card {

    public BlossomingBogbeast() {
        // Whenever this creature attacks, you gain 2 life. Then creatures you control gain trample
        // and get +X/+X until end of turn, where X is the amount of life you gained this turn.
        LifeGainedThisTurn lifeGained = new LifeGainedThisTurn(CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new GainLifeEffect(2),
                new BoostAllOwnCreaturesEffect(lifeGained, lifeGained),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES)
        ));
    }
}
