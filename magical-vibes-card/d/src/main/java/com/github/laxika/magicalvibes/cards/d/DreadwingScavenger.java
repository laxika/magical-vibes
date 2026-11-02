package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "118")
public class DreadwingScavenger extends Card {

    public DreadwingScavenger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), loot());
        addTrigger(Triggers.attacks(), loot());

        GraveyardCardThreshold threshold = new GraveyardCardThreshold(7, null);
        addStaticEffect(new ConditionalEffect(threshold,
                new StaticBoostEffect(1, 1, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(threshold,
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)));
    }

    private SequenceEffect loot() {
        return SequenceEffect.of(
                new DrawCardEffect(1),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }
}
