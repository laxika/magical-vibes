package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "78")
public class DesperateMeasures extends Card {

    public DesperateMeasures() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(1, -1))
                .addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.dies(ObjectRelation.THIS), new DrawCardForTargetPlayerEffect(2)),
                        true));
    }
}
