package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PayManaThenMayTapOrUntapTargetCreatureEffect;

@CardRegistration(set = "M21", collectorNumber = "80")
public class TolarianKraken extends Card {

    public TolarianKraken() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new PayManaThenMayTapOrUntapTargetCreatureEffect("{1}"));
    }
}
