package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerIfSacrificedCardMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "ARN", collectorNumber = "19")
@CardRegistration(set = "ME4", collectorNumber = "61")
public class SerendibDjinn extends Card {

    public SerendibDjinn() {
        // At the beginning of your upkeep, sacrifice a land. If you sacrifice an Island this way,
        // this creature deals 3 damage to you.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificePermanentThenEffect(
                new PermanentIsLandPredicate(),
                new DealDamageToControllerIfSacrificedCardMatchesEffect(
                        new CardSubtypePredicate(CardSubtype.ISLAND), 3),
                "a land", false, false));

        // When you control no lands, sacrifice this creature. State-triggered ability.
        addEffect(EffectSlot.STATE_TRIGGERED, StateTriggerEffect.whenBattlefieldHasAtMost(0,
                new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentIsLandPredicate())),
                List.of(new SacrificeSelfEffect()),
                "Serendib Djinn's state-triggered ability"));
    }
}
