package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TDM", collectorNumber = "219")
public class RiverwheelSweep extends Card {

    public RiverwheelSweep() {
        target(TargetFilters.creature())
                .addSpellEffect(new TapPermanentsEffect(Subjects.target()))
                .addSpellEffect(new PutCounterOnTargetPermanentEffect(CounterType.STUN, 3));
        addSpellEffect(new ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(2));
    }
}
