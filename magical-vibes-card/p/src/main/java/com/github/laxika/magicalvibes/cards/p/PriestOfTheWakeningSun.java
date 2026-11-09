package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "27")
public class PriestOfTheWakeningSun extends Card {

    public PriestOfTheWakeningSun() {
        // At the beginning of your upkeep, you may reveal a Dinosaur card from your hand.
        // If you do, you gain 2 life.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new RevealSubtypeFromHandEffect(CardSubtype.DINOSAUR, new GainLifeEffect(2)),
                "Reveal a Dinosaur card from your hand to gain 2 life?"));

        // {3}{W}{W}, Sacrifice this creature: Search your library for a Dinosaur card,
        // reveal it, put it into your hand, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{W}",
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                new CardSubtypePredicate(CardSubtype.DINOSAUR))
                ),
                "{3}{W}{W}, Sacrifice Priest of the Wakening Sun: Search your library for a Dinosaur card, reveal it, put it into your hand, then shuffle."
        ));
    }
}
