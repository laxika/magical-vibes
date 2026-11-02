package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetIsOpponent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMC", collectorNumber = "20")
@CardRegistration(set = "TMC", collectorNumber = "88")
public class ShredderShadowMaster extends Card {

    public ShredderShadowMaster() {
        addTrigger(Triggers.attacks(), new ConditionalEffect(new AttackedTargetIsOpponent(),
                CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.mandatoryNonLegendary()));
        addTrigger(Triggers.attacks(), new SacrificeCreatedPermanentsAtEndOfCombatEffect());
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new LoseLifeEffect(new HalvedRoundedUp(new TargetPlayerLifeTotal()),
                        LoseLifeRecipient.TARGET_PLAYER));
    }
}
