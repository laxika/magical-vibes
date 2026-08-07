package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "60")
public class JaceVrynsProdigy extends Card {

    public JaceVrynsProdigy() {
        setBackFaceCard(new JaceTelepathUnbound());

        // {T}: Draw a card, then discard a card. If there are five or more cards in your graveyard,
        // exile Jace, then return him to the battlefield transformed under his owner's control.
        // The discard resolves before the count, so the just-discarded card counts toward the five.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                        new ConditionalEffect(
                                new GraveyardCardThreshold(5, new CardTruePredicate()),
                                new ExileSelfAndReturnTransformedEffect())
                ),
                "{T}: Draw a card, then discard a card. If there are five or more cards in your "
                        + "graveyard, exile Jace, Vryn's Prodigy, then return him to the battlefield "
                        + "transformed under his owner's control."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "JaceTelepathUnbound";
    }
}
