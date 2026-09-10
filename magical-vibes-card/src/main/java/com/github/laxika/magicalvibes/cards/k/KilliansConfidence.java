package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOS", collectorNumber = "197")
public class KilliansConfidence extends Card {

    public KilliansConfidence() {
        // Target creature gets +1/+1 until end of turn. Draw a card.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(1, 1))
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(1));

        // Whenever one or more creatures you control deal combat damage to a player, you may pay
        // {W/B}. If you do, return this card from your graveyard to your hand.
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        null,
                        new MayPayManaEffect("{W/B}", new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                                "Pay {W/B} to return Killian's Confidence from your graveyard to your hand?"),
                        false,
                        true));
    }
}
