package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SurvivalTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "DSK", collectorNumber = "89")
public class CynicalLoner extends Card {

    public CynicalLoner() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentHasSubtypePredicate(CardSubtype.GLIMMER)));
        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED, new SurvivalTriggerEffect(
                new ConditionalEffect(
                        new SourceIsTapped(),
                        new MayEffect(
                                new SearchLibraryEffect(null, LibrarySearchDestination.GRAVEYARD),
                                "Search your library for a card and put it into your graveyard?"))));
    }
}
