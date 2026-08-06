package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaToActivePlayerEffect;

@CardRegistration(set = "TMP", collectorNumber = "223")
public class EladamrisVineyard extends Card {

    public EladamrisVineyard() {
        // At the beginning of each player's first main phase, that player adds {G}{G}.
        addEffect(EffectSlot.EACH_PRECOMBAT_MAIN_TRIGGERED,
                new AwardManaToActivePlayerEffect(ManaColor.GREEN, 2));
    }
}
