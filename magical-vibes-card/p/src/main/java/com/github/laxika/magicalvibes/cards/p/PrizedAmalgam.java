package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredOrCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentWasCastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "249")
@CardRegistration(set = "2X2", collectorNumber = "264")
@CardRegistration(set = "TSR", collectorNumber = "382")
@CardRegistration(set = "SIR", collectorNumber = "240")
public class PrizedAmalgam extends Card {

    public PrizedAmalgam() {
        // Whenever a creature enters, if it entered from your graveyard or you cast it from your graveyard, return this
        // card from your graveyard to the battlefield tapped at the beginning of the next end step. A card in your
        // graveyard is yours (CR 400.3); what it came from is fixed as it enters, so it is judged as the ability
        // triggers.
        addTrigger(new TriggerCondition(Triggers.enters(new PermanentAllOfPredicate(List.of(
                        Triggers.CREATURE,
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentEnteredOrCastFromZonePredicate(Zone.GRAVEYARD),
                                        new PermanentNotPredicate(new PermanentWasCastPredicate()))),
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentCastFromZonePredicate(Zone.GRAVEYARD),
                                        new PermanentControlledBySourceControllerPredicate())))))),
                        ObjectRelation.YOU_OWN),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                RegisterDelayedStepTriggerEffect.aboutCardAtNext(
                        Triggers.endStep(ActivePlayer.ANY), Subjects.sourceCard(),
                        new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(true)));
    }
}
