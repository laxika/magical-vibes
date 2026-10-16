package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "DSK", collectorNumber = "139")
public class HandThatFeeds extends Card {

    public HandThatFeeds() {
        // "attacks while there are four or more card types" qualifies the event and is not an
        // intervening "if", so delirium is checked only when the creature attacks.
        addEffect(EffectSlot.ON_ATTACK, ConditionalEffect.atTriggerTime(
                new Delirium(), SequenceEffect.of(
                        new BoostSelfEffect(2, 0),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF))));
    }
}
