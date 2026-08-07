package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.RevealTopXCardsLandsToBattlefieldTappedRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "169")
public class AnimistsAwakening extends Card {

    public AnimistsAwakening() {
        // Reveal the top X cards of your library. Put all land cards from among them onto the
        // battlefield tapped and the rest on the bottom of your library in a random order.
        // Spell mastery — If there are two or more instant and/or sorcery cards in your graveyard,
        // untap those lands.
        addEffect(EffectSlot.SPELL, new RevealTopXCardsLandsToBattlefieldTappedRestOnBottomRandomEffect(
                new GraveyardCardThreshold(2, new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )))));
    }
}
