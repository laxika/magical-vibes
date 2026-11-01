package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "99")
@CardRegistration(set = "CMM", collectorNumber = "185")
public class SerratedScorpion extends Card {

    public SerratedScorpion() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT),
                new GainLifeEffect(2)));
    }
}
