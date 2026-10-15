package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MaestrosGift;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

/** Inspired Skypainter // Maestro's Gift (SOC 48). */
@CardRegistration(set = "SOC", collectorNumber = "48")
@CardRegistration(set = "SOC", collectorNumber = "96")
public class InspiredSkypainterMaestrosGift extends Card {

    public InspiredSkypainterMaestrosGift() {
        setBackFaceCard(new MaestrosGift());

        // This creature enters prepared.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());

        // Whenever one or more creature tokens you control deal combat damage to a player, this
        // creature becomes prepared.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTokenPredicate())),
                        new BecomePreparedEffect(), false, true));
    }

    @Override
    public String getBackFaceClassName() {
        return "MaestrosGift";
    }
}
