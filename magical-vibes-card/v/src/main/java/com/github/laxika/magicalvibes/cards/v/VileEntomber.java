package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "108")
public class VileEntomber extends Card {

    public VileEntomber() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SearchLibraryEffect(null, LibrarySearchDestination.GRAVEYARD));
    }
}
