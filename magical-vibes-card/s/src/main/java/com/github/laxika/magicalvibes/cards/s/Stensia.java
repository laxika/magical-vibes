package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnPerCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OPC2", collectorNumber = "34")
@CardRegistration(set = "OPCA", collectorNumber = "74")
@CardRegistration(set = "MOC", collectorNumber = "159")
public class Stensia extends Card {

    public Stensia() {
        var putCounterOnDamagingCreature = new OncePerTurnPerCreatureTriggerEffect(
                new PutCounterOnReferencedPermanentEffect(
                        PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(Triggers.sourceYouControlDealsDamageToOpponent(), putCounterOnDamagingCreature);
        addTrigger(Triggers.permanentDealsDamageToYou(), putCounterOnDamagingCreature);
        addTrigger(Triggers.creatureDealsDamageToItsController(), putCounterOnDamagingCreature);

        addTrigger(Triggers.chaosEnsues(), new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                        "{T}: This creature deals 1 damage to target player or planeswalker."),
                GrantScope.OWN_CREATURES,
                null,
                EffectDuration.UNTIL_END_OF_TURN));
    }
}
