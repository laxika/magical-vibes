package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.Set;
import com.github.laxika.magicalvibes.model.Zone;

@CardRegistration(set = "BRO", collectorNumber = "85")
public class AshnodsIntervention extends Card {

    public AshnodsIntervention() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 0))
                .addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.dies(ObjectRelation.THIS),
                                new PutCardsIntoHandEffect(Subjects.sourceCard()))))
                .addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, ObjectRelation.THIS),
                                new PutCardsIntoHandEffect(Subjects.triggeringCards()))));
    }
}
