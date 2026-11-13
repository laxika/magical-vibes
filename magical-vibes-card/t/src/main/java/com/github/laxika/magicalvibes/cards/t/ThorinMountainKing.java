package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentsToTargetCreatureThenEffect;
import com.github.laxika.magicalvibes.model.effect.SourcePermanentDealsPowerDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "114")
@CardRegistration(set = "HOC", collectorNumber = "114")
public class ThorinMountainKing extends Card {

    public ThorinMountainKing() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new AttachTargetEquipmentsToTargetCreatureThenEffect(
                                SequenceEffect.upToOneTarget(
                                        new SourcePermanentDealsPowerDamageToTargetCreatureEffect())));
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                "Target must be an Equipment you control"), 0, 99);
    }
}
