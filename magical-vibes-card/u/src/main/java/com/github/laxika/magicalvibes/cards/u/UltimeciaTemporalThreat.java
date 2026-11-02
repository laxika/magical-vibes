package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "441")
@CardRegistration(set = "FIN", collectorNumber = "556")
public class UltimeciaTemporalThreat extends Card {

    public UltimeciaTemporalThreat() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(
                TapUntapScope.ALL_CREATURES,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new AllyCombatDamageTriggerEffect(new PermanentIsCreaturePredicate(), new DrawCardEffect()));
    }
}
