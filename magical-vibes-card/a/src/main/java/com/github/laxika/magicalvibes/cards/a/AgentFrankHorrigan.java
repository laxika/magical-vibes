package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "PIP", collectorNumber = "89")
@CardRegistration(set = "PIP", collectorNumber = "405")
@CardRegistration(set = "PIP", collectorNumber = "617")
@CardRegistration(set = "PIP", collectorNumber = "933")
public class AgentFrankHorrigan extends Card {

    public AgentFrankHorrigan() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceAttackedThisTurn(),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));

        SequenceEffect proliferateTwice = SequenceEffect.of(
                new ProliferateEffect(),
                new ProliferateEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, proliferateTwice);
        addEffect(EffectSlot.ON_ATTACK, proliferateTwice);
    }
}
