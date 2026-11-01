package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "272")
@CardRegistration(set = "UMA", collectorNumber = "243")
@CardRegistration(set = "TSR", collectorNumber = "278")
public class FlagstonesOfTrokair extends Card {

    public FlagstonesOfTrokair() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, ObjectRelation.THIS), new MayEffect(
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.PLAINS),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                "Search your library for a Plains card?"));
    }
}
