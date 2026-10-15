package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringNonTapAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ATQ", collectorNumber = "15")
public class ArtifactPossession extends Card {

    public ArtifactPossession() {
        target(TargetFilters.artifact());
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED,
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));

        CardEffect activationTrigger = new TriggeringNonTapAbilityConditionalEffect(
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsHostOfSourceAuraPredicate(),
                        new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER)));
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_ABILITY, activationTrigger);
        addEffect(EffectSlot.ON_OPPONENT_ACTIVATES_ABILITY, activationTrigger);
    }
}
