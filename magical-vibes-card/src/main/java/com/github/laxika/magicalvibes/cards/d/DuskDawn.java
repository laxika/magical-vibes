package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

/**
 * Dusk // Dawn — front half (Dusk).
 * Sorcery — Destroy all creatures with power 3 or greater.
 * Back half (Dawn) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "AKH", collectorNumber = "210")
@CardRegistration(set = "AKR", collectorNumber = "16")
public class DuskDawn extends Card {

    public DuskDawn() {
        setBackFaceCard(new Dawn());

        // Destroy all creatures with power 3 or greater.
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(3)))));
    }

    @Override
    public String getBackFaceClassName() {
        return "Dawn";
    }
}
