package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "169")
@CardRegistration(set = "SLD", collectorNumber = "934")
@CardRegistration(set = "SLD", collectorNumber = "1200")
@CardRegistration(set = "2XM", collectorNumber = "128")
@CardRegistration(set = "FCA", collectorNumber = "13")
@CardRegistration(set = "CMM", collectorNumber = "227")
@CardRegistration(set = "CMM", collectorNumber = "537")
@CardRegistration(set = "C16", collectorNumber = "125")
public class GodoBanditWarlord extends Card {

    public GodoBanditWarlord() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                        LibrarySearchDestination.BATTLEFIELD),
                        "Search your library for an Equipment card?"));

        // "Whenever Godo attacks for the first time each turn": every half is limited to once each turn
        // (TriggerLimit.ONCE_EACH_TURN), so attacking again in the extra combat phase this grants doesn't loop. Godo is
        // not a Samurai, so untapping itself is a separate SELF scope from the Samurai sweep.
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new UntapPermanentsEffect(TapUntapScope.SELF),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                        new PermanentHasSubtypePredicate(CardSubtype.SAMURAI)),
                new AdditionalCombatPhaseEffect(1));
    }
}
