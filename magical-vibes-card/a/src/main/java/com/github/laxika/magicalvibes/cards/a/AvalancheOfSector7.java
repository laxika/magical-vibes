package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "FIC", collectorNumber = "53")
@CardRegistration(set = "FIC", collectorNumber = "150")
public class AvalancheOfSector7 extends Card {

    public AvalancheOfSector7() {
        PermanentCount opponentArtifacts =
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.OPPONENTS);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(opponentArtifacts, new Fixed(3)));
        addEffect(EffectSlot.ON_OPPONENT_ACTIVATES_ABILITY, new TriggeringPermanentConditionalEffect(
                new PermanentIsArtifactPredicate(),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER)));
    }
}
