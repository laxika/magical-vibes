package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "576")
public class CoalHillSchool extends Card {

    public CoalHillSchool() {
        // Whenever a player casts a historic spell, that player draws a card.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                SpellCastTriggerEffect.anyPlayer(new CardIsHistoricPredicate(),
                        List.of(new DrawCardForTargetPlayerEffect(1))));

        // Whenever chaos ensues, return target historic card from your graveyard to your hand.
        addEffect(EffectSlot.CHAOS_TRIGGERED, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardIsHistoricPredicate())
                .targetGraveyard(true)
                .build());
    }
}
