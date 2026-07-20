package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchZonesForCardNamedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "228")
public class GateToTheAfterlife extends Card {

    public GateToTheAfterlife() {
        // Whenever a nontoken creature you control dies, you gain 1 life. Then you may draw a card.
        // If you do, discard a card.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, SequenceEffect.of(
                new GainLifeEffect(1),
                new MayEffect(
                        SequenceEffect.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                        "Draw a card and discard a card?")
        ));

        // {2}, {T}, Sacrifice this artifact: Search your graveyard, hand, and/or library for a card
        // named God-Pharaoh's Gift and put it onto the battlefield. If you search your library this
        // way, shuffle. Activate only if there are six or more creature cards in your graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(),
                        new SearchZonesForCardNamedToBattlefieldEffect("God-Pharaoh's Gift")),
                "{2}, {T}, Sacrifice Gate to the Afterlife: Search your graveyard, hand, and/or library "
                        + "for a card named God-Pharaoh's Gift and put it onto the battlefield. If you search "
                        + "your library this way, shuffle. Activate only if there are six or more creature "
                        + "cards in your graveyard.")
                .withRequiredGraveyardCards(new CardTypePredicate(CardType.CREATURE), 6, "creature cards in your graveyard"));
    }
}
