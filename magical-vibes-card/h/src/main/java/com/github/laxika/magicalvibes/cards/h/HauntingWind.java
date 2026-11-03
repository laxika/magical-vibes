package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ATQ", collectorNumber = "17")
public class HauntingWind extends Card {

    public HauntingWind() {
        CardEffect artifactTapTrigger = new TriggeringPermanentConditionalEffect(
                new PermanentIsArtifactPredicate(),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
        addTrigger(Triggers.permanentYouControlBecomesTapped(), artifactTapTrigger);
        addTrigger(Triggers.permanentOpponentControlsBecomesTapped(), artifactTapTrigger);

        CardEffect artifactActivationTrigger =
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER);
        addTrigger(Triggers.youActivateAbility()
                        .withKind(AbilityTrigger.AbilityKind.WITHOUT_TAP_COST)
                        .withSource(new PermanentIsArtifactPredicate()),
                artifactActivationTrigger);
        addTrigger(Triggers.opponentActivatesAbility()
                        .withKind(AbilityTrigger.AbilityKind.WITHOUT_TAP_COST)
                        .withSource(new PermanentIsArtifactPredicate()),
                artifactActivationTrigger);
    }
}
