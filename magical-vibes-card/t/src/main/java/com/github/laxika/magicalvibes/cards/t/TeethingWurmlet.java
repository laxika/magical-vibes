package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceIfFirstResolutionThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "BRO", collectorNumber = "192")
public class TeethingWurmlet extends Card {

    public TeethingWurmlet() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new Metalcraft(),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)));
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, SequenceEffect.of(
                new GainLifeEffect(1),
                new PutCountersOnSourceIfFirstResolutionThisTurnEffect(
                        "artifact-enters", CounterType.PLUS_ONE_PLUS_ONE, 1)));
    }
}
