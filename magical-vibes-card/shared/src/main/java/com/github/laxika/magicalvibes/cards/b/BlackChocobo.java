package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class BlackChocobo extends Card {

    public BlackChocobo() {
        addTrigger(Triggers.transformsToBackFace(ObjectRelation.THIS),
                new SearchLibraryEffect(new CardTypePredicate(CardType.LAND),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED));
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new BoostAllOwnCreaturesEffect(1, 0,
                        new PermanentHasSubtypePredicate(CardSubtype.BIRD)));
    }
}
