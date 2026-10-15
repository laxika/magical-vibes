package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEqualToLifeLostThisTurn;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantGainLifeEffect;

@CardRegistration(set = "CMM", collectorNumber = "137")
@CardRegistration(set = "CMM", collectorNumber = "503")
public class ArchfiendOfDespair extends Card {

    public ArchfiendOfDespair() {
        addEffect(EffectSlot.STATIC, new OpponentsCantGainLifeEffect());
        addEffect(EffectSlot.END_STEP_TRIGGERED, new EachOpponentLosesLifeEqualToLifeLostThisTurn());
    }
}
