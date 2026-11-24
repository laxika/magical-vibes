package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "98")
@CardRegistration(set = "THB", collectorNumber = "314")
@CardRegistration(set = "SLD", collectorNumber = "953")
@CardRegistration(set = "SLD", collectorNumber = "954")
@CardRegistration(set = "SLD", collectorNumber = "1643")
public class GravebreakerLamia extends Card {

    public GravebreakerLamia() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SearchLibraryEffect(null, LibrarySearchDestination.GRAVEYARD));
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardTruePredicate(), 1, CostModificationScope.SELF, Zone.GRAVEYARD));
    }
}
