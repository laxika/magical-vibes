package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "49")
@CardRegistration(set = "ACR", collectorNumber = "141")
public class BasimIbnIshaq extends Card {

    public BasimIbnIshaq() {
        // Whenever you cast a historic spell, draw a card. Basim Ibn Ishaq can't be blocked this
        // turn. This ability triggers only once each turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new OncePerTurnTriggerEffect(
                new SpellCastTriggerEffect(
                        new CardIsHistoricPredicate(),
                        List.of(new DrawCardEffect(), new MakeCreatureUnblockableEffect(true))
                )));

        // Whenever Basim Ibn Ishaq deals combat damage to a player, put a +1/+1 counter on it.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
