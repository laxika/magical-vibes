package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "226")
@CardRegistration(set = "HBG", collectorNumber = "239")
public class KrydleOfBaldursGate extends Card {

    public KrydleOfBaldursGate() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
                new MillEffect(1, MillRecipient.TARGET_PLAYER),
                new GainLifeEffect(1),
                new ScryEffect(1)));

        target(TargetFilters.creature()).addTrigger(Triggers.youAttack(),
                new MayPayManaEffect("{2}", new MakeCreatureUnblockableEffect(),
                        "Pay {2} to make target creature unable to be blocked?"));
    }
}
