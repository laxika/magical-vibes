package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "ALL", collectorNumber = "51a")
@CardRegistration(set = "ALL", collectorNumber = "51b")
public class InsidiousBookworms extends Card {

    public InsidiousBookworms() {
        // When this creature dies, you may pay {1}{B}. If you do, target player discards a card at random.
        addEffect(EffectSlot.ON_DEATH, new MayPayManaEffect("{1}{B}",
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, true),
                "pay {1}{B}"));
    }
}
