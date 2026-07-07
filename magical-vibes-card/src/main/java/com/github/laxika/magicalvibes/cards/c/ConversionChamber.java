package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NPH", collectorNumber = "133")
public class ConversionChamber extends Card {

    public ConversionChamber() {
        // {2}, {T}: Exile target artifact card from a graveyard. Put a charge counter on Conversion Chamber.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD,
                                new CardTypePredicate(CardType.ARTIFACT)),
                        new PutCountersOnSelfEffect(CounterType.CHARGE)
                ),
                "{2}, {T}: Exile target artifact card from a graveyard. Put a charge counter on Conversion Chamber."
        ));

        // {2}, {T}, Remove a charge counter from Conversion Chamber: Create a 3/3 colorless Phyrexian Golem artifact creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new RemoveChargeCountersFromSourceCost(1),
                        new CreateTokenEffect("Golem", 3, 3, null,
                                List.of(CardSubtype.PHYREXIAN, CardSubtype.GOLEM), Set.of(), Set.of(CardType.ARTIFACT))
                ),
                "{2}, {T}, Remove a charge counter from Conversion Chamber: Create a 3/3 colorless Phyrexian Golem artifact creature token."
        ));
    }
}
