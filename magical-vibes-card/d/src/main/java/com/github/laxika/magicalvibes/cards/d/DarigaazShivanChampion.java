package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureDarigaazShivanChampionSpellbookEffect;

@CardRegistration(set = "YDMU", collectorNumber = "22")
public class DarigaazShivanChampion extends Card {

    public DarigaazShivanChampion() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConjureDarigaazShivanChampionSpellbookEffect());
    }
}
