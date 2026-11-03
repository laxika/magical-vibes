package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "53")
@CardRegistration(set = "FIC", collectorNumber = "150")
public class AvalancheOfSector7 extends Card {

    public AvalancheOfSector7() {
        PermanentCount opponentArtifacts =
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.OPPONENTS);
        addStaticEffect(new SetPowerToughnessToAmountEffect(opponentArtifacts, new Fixed(3)));
        addTrigger(Triggers.opponentActivatesAbility().withSource(new PermanentIsArtifactPredicate()),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
