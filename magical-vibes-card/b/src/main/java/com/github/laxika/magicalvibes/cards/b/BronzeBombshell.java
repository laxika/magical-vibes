package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.UUID;

@CardRegistration(set = "DIS", collectorNumber = "160")
public class BronzeBombshell extends Card {

    public BronzeBombshell() {
        addTrigger(Triggers.state((gameData, sourcePermanent, controllerId) -> {
                    UUID ownerId = sourcePermanent.getCard().getOwnerId();
                    return ownerId != null && !ownerId.equals(controllerId);
                }),
                SequenceEffect.of(new SacrificeSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new DealDamageToPlayersEffect(7, Subjects.you()))));
    }
}
