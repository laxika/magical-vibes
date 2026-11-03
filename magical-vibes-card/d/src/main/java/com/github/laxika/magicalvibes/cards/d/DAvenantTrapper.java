package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "11")
public class DAvenantTrapper extends Card {

    public DAvenantTrapper() {
        // Whenever you cast a historic spell, tap target creature an opponent controls.
        // (Artifacts, legendaries, and Sagas are historic.)
        target(TargetFilters.creatureAnOpponentControls()).addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardIsHistoricPredicate()),
                new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
