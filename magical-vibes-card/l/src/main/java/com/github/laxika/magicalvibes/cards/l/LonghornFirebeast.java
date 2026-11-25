package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "103")
public class LonghornFirebeast extends Card {

    public LonghornFirebeast() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), UnlessPaysEffect.mayPay(Subjects.eachOpponent(), DamageFromSourceCost.of(5),
                new SacrificeSelfEffect())
                .withPrompt("Have it deal 5 damage to you? If a player does, it is sacrificed.")
                .payingTogether());
    }
}
