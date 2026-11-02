package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.EquipmentTapsAndLocksDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "155")
public class NekoTe extends Card {

    public NekoTe() {
        addTrigger(Triggers.creatureYouControlDealsDamageToCreature(),
                new EquipmentTapsAndLocksDamagedCreatureEffect());
        addTrigger(Triggers.dealsDamageToPlayer(),
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
