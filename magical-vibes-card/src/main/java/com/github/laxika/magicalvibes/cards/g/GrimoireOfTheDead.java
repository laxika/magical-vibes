package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "226")
public class GrimoireOfTheDead extends Card {

    public GrimoireOfTheDead() {
        // {1}, {T}, Discard a card: Put a study counter on Grimoire of the Dead.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DiscardCardTypeCost(null, null), new PutCounterOnSelfEffect(CounterType.STUDY)),
                "{1}, {T}, Discard a card: Put a study counter on Grimoire of the Dead."
        ));

        // {T}, Remove three study counters from Grimoire of the Dead and sacrifice it:
        // Put all creature cards from all graveyards onto the battlefield under your control.
        // They're black Zombies in addition to their other colors and types.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.STUDY),
                        new SacrificeSelfCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                                .returnAll(true)
                                .grantColor(CardColor.BLACK)
                                .grantSubtype(CardSubtype.ZOMBIE)
                                .build()
                ),
                "{T}, Remove three study counters from Grimoire of the Dead and sacrifice it: Put all creature cards from all graveyards onto the battlefield under your control. They're black Zombies in addition to their other colors and types."
        ));
    }
}
