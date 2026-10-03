package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisResolution;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "C16", collectorNumber = "27")
public class AncientExcavation extends Card {

    public AncientExcavation() {
        // Draw cards equal to the number of cards in your hand, then discard a card for each card
        // drawn this way.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new CardsInHand(CountScope.CONTROLLER)));
        addEffect(EffectSlot.SPELL,
                new DiscardEffect(new CardsDrawnThisResolution(), DiscardRecipient.CONTROLLER));

        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "Basic landcycling {2} ({2}, Discard this card: Search your library for a basic land "
                        + "card, reveal it, put it into your hand, then shuffle.)"));
    }
}
