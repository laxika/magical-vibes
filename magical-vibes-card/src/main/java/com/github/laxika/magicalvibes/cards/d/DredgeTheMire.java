package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesCreatureCardFromTheirGraveyardToBattlefieldEffect;

@CardRegistration(set = "C20", collectorNumber = "43")
@CardRegistration(set = "SCD", collectorNumber = "76")
public class DredgeTheMire extends Card {

    public DredgeTheMire() {
        addEffect(EffectSlot.SPELL, new EachOpponentChoosesCreatureCardFromTheirGraveyardToBattlefieldEffect());
    }
}
