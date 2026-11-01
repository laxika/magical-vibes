package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "150")
@CardRegistration(set = "LCC", collectorNumber = "228")
public class MaraudingRaptor extends Card {

    public MaraudingRaptor() {
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardTypePredicate(CardType.CREATURE), 1, CostModificationScope.SELF));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect(
                        2, new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR), 2, 0));
    }
}
