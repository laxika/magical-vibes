package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentMayPlayWithOpponentTaxEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "11")
@CardRegistration(set = "NCC", collectorNumber = "112")
public class AerialExtortionist extends Card {

    public AerialExtortionist() {
        target(TargetFilters.nonlandPermanent(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentMayPlayWithOpponentTaxEffect(0))
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new ExileTargetPermanentMayPlayWithOpponentTaxEffect(0));

        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        null,
                        List.of(new DrawCardEffect()),
                        new StackEntryNotPredicate(new StackEntryCastFromZonePredicate(Zone.HAND))));
    }
}
