package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "33")
@CardRegistration(set = "MOC", collectorNumber = "120")
public class PainDistributor extends Card {

    public PainDistributor() {
        addTrigger(Triggers.playerCastsSpell().nthThisTurn(1),
                new CreateTokenForTriggeringPlayerEffect(CreateTokenEffect.ofTreasureToken(1)));
        addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.OPPONENT_CONTROLS),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
