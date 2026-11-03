package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ATQ", collectorNumber = "15")
public class ArtifactPossession extends Card {

    public ArtifactPossession() {
        target(TargetFilters.artifact());
        addTrigger(Triggers.enchantedPermanentBecomesTapped(),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));

        CardEffect activationTrigger =
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER);
        addTrigger(Triggers.youActivateAbility()
                        .withKind(AbilityTrigger.AbilityKind.WITHOUT_TAP_COST)
                        .withSource(new PermanentIsHostOfSourceAuraPredicate()),
                activationTrigger);
        addTrigger(Triggers.opponentActivatesAbility()
                        .withKind(AbilityTrigger.AbilityKind.WITHOUT_TAP_COST)
                        .withSource(new PermanentIsHostOfSourceAuraPredicate()),
                activationTrigger);
    }
}
