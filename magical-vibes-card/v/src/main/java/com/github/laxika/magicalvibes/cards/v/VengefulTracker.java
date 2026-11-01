package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "147")
public class VengefulTracker extends Card {

    public VengefulTracker() {
        addTrigger(Triggers.sacrificed(null, ObjectRelation.OPPONENT_CONTROLS),
                new TriggeringPermanentConditionalEffect(
                new PermanentIsArtifactPredicate(),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PLAYER)));
    }
}
