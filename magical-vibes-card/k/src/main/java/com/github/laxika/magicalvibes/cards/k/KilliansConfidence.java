package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOS", collectorNumber = "197")
public class KilliansConfidence extends Card {

    public KilliansConfidence() {
        // Target creature gets +1/+1 until end of turn. Draw a card.
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(1, 1))
                .addSpellEffect(new DrawCardEffect(1));

        // Whenever one or more creatures you control deal combat damage to a player, you may pay
        // {W/B}. If you do, return this card from your graveyard to your hand.
        addTrigger(new TriggerCondition(Triggers.creatureYouControlDealsCombatDamageToPlayer(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new AllyCombatDamageTriggerEffect(
                        null,
                        new MayPayManaEffect("{W/B}", new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                                "Pay {W/B} to return Killian's Confidence from your graveyard to your hand?"),
                        false,
                        true));
    }
}
