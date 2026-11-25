package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "213")
@CardRegistration(set = "KLR", collectorNumber = "240")
public class FireforgersPuzzleknot extends Card {

    public FireforgersPuzzleknot() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), DealDamageEffect.toAnyTarget(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new SacrificeSelfCost(), DealDamageEffect.toAnyTarget(1)),
                "{2}{R}, Sacrifice this artifact: It deals 1 damage to any target."
        ));
    }
}
