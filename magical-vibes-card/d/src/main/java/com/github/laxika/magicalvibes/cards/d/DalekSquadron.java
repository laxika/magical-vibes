package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;

@CardRegistration(set = "WHO", collectorNumber = "65")
@CardRegistration(set = "WHO", collectorNumber = "538")
@CardRegistration(set = "WHO", collectorNumber = "670")
@CardRegistration(set = "WHO", collectorNumber = "1129")
public class DalekSquadron extends Card {

    public DalekSquadron() {
        addEffect(EffectSlot.ON_ATTACK,
                CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.myriad());
    }
}
