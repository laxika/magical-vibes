package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureTypeWithBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "1")
public class AgelessSentinels extends Card {

    public AgelessSentinels() {
        addTrigger(Triggers.blocks(), SequenceEffect.of(
                BecomeCreatureTypeWithBasePowerToughnessEffect.replacingSubtype(
                        CardSubtype.BIRD, null, CardSubtype.WALL),
                new BecomeCreatureTypeWithBasePowerToughnessEffect(
                        null, null, CardSubtype.GIANT, null, false, false, null),
                new RemoveKeywordEffect(Keyword.DEFENDER, GrantScope.SELF, EffectDuration.PERMANENT)));
    }
}
