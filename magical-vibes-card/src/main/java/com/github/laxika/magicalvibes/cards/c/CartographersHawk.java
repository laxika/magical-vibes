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
public class CartographersHawk extends Card {

    public CartographersHawk() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ConditionalEffect(
                        new TargetPlayerControlsMoreLandsThanController(),
                        new ReturnSelfToHandThenEffect(
                                new MayEffect(
                                        new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.PLAINS),
                                                LibrarySearchDestination.BATTLEFIELD_TAPPED),
                                        "Search your library for a Plains card?"))));
    }
}
