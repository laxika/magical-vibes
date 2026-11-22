package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSearchesLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "144")
@CardRegistration(set = "2XM", collectorNumber = "186")
@CardRegistration(set = "CMD", collectorNumber = "177")
@CardRegistration(set = "MB2", collectorNumber = "76")
@CardRegistration(set = "C16", collectorNumber = "174")
public class VeteranExplorer extends Card {

    public VeteranExplorer() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), EachPlayerSearchesLibraryEffect.maySearch(
                Subjects.eachPlayer(), CardPredicateUtils.basicLand(), new Fixed(2),
                LibrarySearchDestination.BATTLEFIELD));
    }
}
