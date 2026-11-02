package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "14")
@CardRegistration(set = "THB", collectorNumber = "20")
@CardRegistration(set = "UMA", collectorNumber = "19")
@CardRegistration(set = "PIO", collectorNumber = "15")
public class HeliodsPilgrim extends Card {

    public HeliodsPilgrim() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new SearchLibraryEffect(new CardIsAuraPredicate()),
                        "Search your library for an Aura card?"));
    }
}
