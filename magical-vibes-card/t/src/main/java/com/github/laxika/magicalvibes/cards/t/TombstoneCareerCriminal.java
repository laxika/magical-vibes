package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "70")
@CardRegistration(set = "OM1", collectorNumber = "63")
@CardRegistration(set = "MSC", collectorNumber = "160")
@CardRegistration(set = "MSC", collectorNumber = "577")
public class TombstoneCareerCriminal extends Card {

    public TombstoneCareerCriminal() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(
                new CardSubtypePredicate(CardSubtype.VILLAIN)));
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.VILLAIN), 1, CostModificationScope.SELF));
    }
}
