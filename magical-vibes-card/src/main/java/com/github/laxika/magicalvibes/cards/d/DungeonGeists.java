package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "DKA", collectorNumber = "36")
@CardRegistration(set = "M20", collectorNumber = "57")
public class DungeonGeists extends Card {

    public DungeonGeists() {
        // Flying is loaded from Scryfall.

        // When this creature enters, tap target creature an opponent controls. That creature
        // doesn't untap during its controller's untap step for as long as you control this creature.
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, DoesntUntapEffect.targetWhileSourceOnBattlefield());
    }
}
