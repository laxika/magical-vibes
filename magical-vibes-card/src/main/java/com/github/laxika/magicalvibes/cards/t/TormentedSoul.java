package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "M12", collectorNumber = "114")
public class TormentedSoul extends Card {

    public TormentedSoul() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
