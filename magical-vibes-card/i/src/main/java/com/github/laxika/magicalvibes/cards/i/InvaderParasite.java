package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndImprintEffect;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardNameMatchesEnteringPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "87")
public class InvaderParasite extends Card {

    public InvaderParasite() {
        // Imprint — When Invader Parasite enters the battlefield, exile target land.
        target(TargetFilters.land()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTargetPermanentAndImprintEffect());

        // Whenever a land with the same name as the exiled card enters the battlefield
        // under an opponent's control, Invader Parasite deals 2 damage to that player.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.OPPONENT_CONTROLS),
                new ConditionalEffect(new ImprintedCardNameMatchesEnteringPermanent(), 
                        new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER)
                ));
    }
}
