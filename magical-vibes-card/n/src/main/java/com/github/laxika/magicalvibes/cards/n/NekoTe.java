package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.EquipmentTapsAndLocksDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "BOK", collectorNumber = "155")
public class NekoTe extends Card {

    public NekoTe() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                new EquipmentTapsAndLocksDamagedCreatureEffect());
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER,
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
