package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "AVR", collectorNumber = "135")
public class FerventCathar extends Card {

    public FerventCathar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
