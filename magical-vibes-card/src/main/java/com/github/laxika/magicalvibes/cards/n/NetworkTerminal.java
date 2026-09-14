package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "251")
public class NetworkTerminal extends Card {

    public NetworkTerminal() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentIsArtifactPredicate(), true),
                        new DrawCardEffect(),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)
                ),
                "{1}, {T}, Tap another untapped artifact you control: Draw a card, then discard a card."
        ));
    }
}
