package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "97")
@CardRegistration(set = "2XM", collectorNumber = "101")
@CardRegistration(set = "PIO", collectorNumber = "337")
public class OvalchaseDaredevil extends Card {

    public OvalchaseDaredevil() {
        // Whenever an artifact you control enters, you may return this card from your graveyard to your hand.
        addTrigger(new TriggerCondition(Triggers.enters(
                                new PermanentAllOfPredicate(List.of(
                                        Triggers.ARTIFACT,
                                        new PermanentIsArtifactPredicate())),
                                ObjectRelation.YOU_CONTROL),
                        AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new MayEffect(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build(),
                        "Return Ovalchase Daredevil from your graveyard to your hand?"));
    }
}
