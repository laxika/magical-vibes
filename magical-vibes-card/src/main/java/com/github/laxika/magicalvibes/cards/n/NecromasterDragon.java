package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "DTK", collectorNumber = "226")
public class NecromasterDragon extends Card {

    public NecromasterDragon() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayPayManaEffect("{2}",
                        SequenceEffect.of(
                                CreateTokenEffect.blackZombie(1),
                                new MillEffect(2, MillRecipient.EACH_OPPONENT)),
                        "Pay {2} to create a Zombie token and mill each opponent two cards?"));
    }
}
