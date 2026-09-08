package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerChoosesCreatureToBlockEffect;

@CardRegistration(set = "EXO", collectorNumber = "108")
@CardRegistration(set = "TPR", collectorNumber = "168")
@CardRegistration(set = "BTD", collectorNumber = "52")
public class CrashingBoars extends Card {

    public CrashingBoars() {
        addEffect(EffectSlot.ON_ATTACK, new DefendingPlayerChoosesCreatureToBlockEffect());
    }
}
