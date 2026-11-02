package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "161")
public class GibberingFiend extends Card {

    public GibberingFiend() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                new ConditionalEffect(new Delirium(),
                        new DealDamageToPlayersEffect(1, DamageRecipient.ACTIVE_PLAYER)));
    }
}
