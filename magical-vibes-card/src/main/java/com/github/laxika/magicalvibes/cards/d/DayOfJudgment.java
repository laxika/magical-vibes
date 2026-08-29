package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "12")
@CardRegistration(set = "M12", collectorNumber = "12")
@CardRegistration(set = "ZEN", collectorNumber = "9")
@CardRegistration(set = "FDN", collectorNumber = "140")
public class DayOfJudgment extends Card {

    public DayOfJudgment() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
