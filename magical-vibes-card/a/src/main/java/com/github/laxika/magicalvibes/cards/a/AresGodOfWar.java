package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "202")
public class AresGodOfWar extends Card {

    public AresGodOfWar() {
        addStaticEffect(new MustAttackEffect());

        addTrigger(Triggers.creatureDies(new PermanentIsAttackingPredicate(), ObjectRelation.YOU_CONTROL),
                new ReturnTriggeringCardToOwnerHandEffect());

    }
}
