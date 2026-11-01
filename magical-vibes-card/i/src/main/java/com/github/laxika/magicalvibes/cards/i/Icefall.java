package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "85")
public class Icefall extends Card {

    public Icefall() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsLandPredicate()
                )),
                "Target must be an artifact or land"
        )).addSpellEffect(new DestroyTargetPermanentEffect(false));

        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.YOU_OWN),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new MayPayManaEffect("{R}{R}",
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Pay {R}{R} to return Icefall from your graveyard to your hand?",
                        new ExileSourceCardFromGraveyardEffect()));
    }
}
