package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "226")
public class NecromasterDragon extends Card {

    public NecromasterDragon() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayPayManaEffect("{2}",
                        SequenceEffect.of(
                                CreateTokenEffect.blackZombie(1),
                                new MillEffect(2, MillRecipient.EACH_OPPONENT)),
                        "Pay {2} to create a Zombie token and mill each opponent two cards?"));
    }
}
