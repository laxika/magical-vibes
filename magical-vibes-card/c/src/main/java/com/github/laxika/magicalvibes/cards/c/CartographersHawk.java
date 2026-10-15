package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsMoreLandsThanController;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "CMM", collectorNumber = "18")
@CardRegistration(set = "C20", collectorNumber = "24")
@CardRegistration(set = "SCD", collectorNumber = "10")
public class CartographersHawk extends Card {

    public CartographersHawk() {
        // "to a player who controls more lands than you" qualifies the event; it isn't rechecked
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                ConditionalEffect.atTriggerTime(
                        new TargetPlayerControlsMoreLandsThanController(),
                        new ReturnSelfToHandThenEffect(
                                new MayEffect(
                                        new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.PLAINS),
                                                LibrarySearchDestination.BATTLEFIELD_TAPPED),
                                        "Search your library for a Plains card?"))));
    }
}
