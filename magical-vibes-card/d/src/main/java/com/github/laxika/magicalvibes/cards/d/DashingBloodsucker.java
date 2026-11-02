package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "90")
public class DashingBloodsucker extends Card {

    public DashingBloodsucker() {
        SequenceEffect eerie = SequenceEffect.of(
                new BoostSelfEffect(2, 0),
                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF));
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL), eerie);
        addTrigger(Triggers.youFullyUnlockRoom(), eerie);
    }
}
