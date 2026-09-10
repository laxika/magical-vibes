package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RememberTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SetOpponentMaximumHandSizeEffect;

@CardRegistration(set = "4ED", collectorNumber = "312")
@CardRegistration(set = "ATQ", collectorNumber = "48")
@CardRegistration(set = "ME1", collectorNumber = "155")
public class CursedRack extends Card {

    public CursedRack() {
        // "As this artifact enters, choose an opponent. The chosen player's maximum hand size is four."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RememberTargetPlayerEffect());
        addEffect(EffectSlot.STATIC, new SetOpponentMaximumHandSizeEffect(4));
    }
}
