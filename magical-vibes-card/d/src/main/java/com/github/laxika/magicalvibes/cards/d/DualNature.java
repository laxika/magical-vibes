package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnteringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTokensCreatedWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTokensWithSameNameAsLeavingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "112")
public class DualNature extends Card {

    public DualNature() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardIsTokenPredicate()),
                        new CreateTokenCopyOfEnteringPermanentEffect(false, false, false, true, true)));
        addTrigger(Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new ExileTokensWithSameNameAsLeavingCreatureEffect());
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new ExileTokensCreatedWithSourceEffect());
    }
}
