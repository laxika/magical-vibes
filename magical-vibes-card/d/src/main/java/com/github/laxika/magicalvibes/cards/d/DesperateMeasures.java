package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "78")
public class DesperateMeasures extends Card {

    public DesperateMeasures() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(1, -1))
                .addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.dies(ObjectRelation.THIS)
                                .withFilter(new PermanentControlledBySourceControllerPredicate()),
                                new DrawCardEffect(2)),
                        true));
    }
}
