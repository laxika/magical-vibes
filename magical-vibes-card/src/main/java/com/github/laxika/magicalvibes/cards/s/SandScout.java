package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreLands;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTC", collectorNumber = "11")
@CardRegistration(set = "OTC", collectorNumber = "47")
public class SandScout extends Card {

    public SandScout() {
        // When this creature enters, if an opponent controls more lands than you, search your
        // library for a Desert card, put it onto the battlefield tapped, then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new OpponentControlsMoreLands(),
                        new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.DESERT),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)));

        // Whenever one or more land cards are put into your graveyard from anywhere, create a 1/1
        // red, green, and white Sand Warrior creature token. This ability triggers only once each turn.
        addEffect(EffectSlot.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new OncePerTurnTriggerEffect(new CreateTokenEffect("Sand Warrior", 1, 1,
                        CardColor.RED, Set.of(CardColor.RED, CardColor.GREEN, CardColor.WHITE),
                        List.of(CardSubtype.WARRIOR))));
    }
}
