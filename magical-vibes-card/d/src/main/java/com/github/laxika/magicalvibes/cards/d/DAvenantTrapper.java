package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "11")
public class DAvenantTrapper extends Card {

    public DAvenantTrapper() {
        // Whenever you cast a historic spell, tap target creature an opponent controls.
        // (Artifacts, legendaries, and Sagas are historic.)
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardIsHistoricPredicate(),
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                null,
                TargetFilters.creatureAnOpponentControls()
        ));
    }
}
