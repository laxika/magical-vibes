package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "33")
@CardRegistration(set = "MOC", collectorNumber = "120")
public class PainDistributor extends Card {

    public PainDistributor() {
        addTrigger(Triggers.playerCastsSpell().nthThisTurn(1),
                new CreateTokenForTriggeringPlayerEffect(CreateTokenEffect.ofTreasureToken(1)));
        addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.OPPONENT_CONTROLS),
                DealDamageEffect.to(Subjects.controllerOf(Subjects.triggering()), 1));
    }
}
