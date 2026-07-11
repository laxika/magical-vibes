package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PlayTargetCardFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "249")
public class HordeOfNotions extends Card {

    public HordeOfNotions() {
        // Vigilance, trample, haste come from Scryfall metadata.
        // {W}{U}{B}{R}{G}: You may play target Elemental card from your graveyard without paying its mana cost.
        // Changelings are Elemental cards too, so they qualify.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{U}{B}{R}{G}",
                List.of(new PlayTargetCardFromGraveyardWithoutPayingManaCostEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.ELEMENTAL),
                                new CardKeywordPredicate(Keyword.CHANGELING))))),
                "{W}{U}{B}{R}{G}: You may play target Elemental card from your graveyard without paying its mana cost."
        ));
    }
}
