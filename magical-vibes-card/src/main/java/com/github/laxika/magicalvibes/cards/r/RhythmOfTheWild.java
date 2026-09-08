package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesHaveRiotEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCreatureSpellsCantBeCounteredEffect;

@CardRegistration(set = "RNA", collectorNumber = "201")
public class RhythmOfTheWild extends Card {

    public RhythmOfTheWild() {
        addEffect(EffectSlot.STATIC, new ControllerCreatureSpellsCantBeCounteredEffect());
        addEffect(EffectSlot.STATIC, new ControlledCreaturesHaveRiotEffect());
    }
}
