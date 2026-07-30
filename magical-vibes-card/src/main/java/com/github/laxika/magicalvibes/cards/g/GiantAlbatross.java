package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "HML", collectorNumber = "27a")
@CardRegistration(set = "HML", collectorNumber = "27b")
public class GiantAlbatross extends Card {

    public GiantAlbatross() {
        // When this creature dies, you may pay {1}{U}. If you do, for each creature that dealt damage
        // to this creature this turn, destroy that creature unless its controller pays 2 life. A
        // creature destroyed this way can't be regenerated.
        addEffect(EffectSlot.ON_DEATH, new MayPayManaEffect("{1}{U}",
                new DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect(2),
                "Pay {1}{U} to punish the creatures that damaged this creature?"));
    }
}
