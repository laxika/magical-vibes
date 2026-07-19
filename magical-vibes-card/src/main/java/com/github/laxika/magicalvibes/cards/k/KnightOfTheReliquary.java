package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "113")
public class KnightOfTheReliquary extends Card {

    public KnightOfTheReliquary() {
        // This creature gets +1/+1 for each land card in your graveyard.
        CardsInGraveyard landCardsInGraveyard =
                new CardsInGraveyard(new CardTypePredicate(CardType.LAND), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(landCardsInGraveyard, landCardsInGraveyard));

        // {T}, Sacrifice a Forest or Plains:
        // Search your library for a land card, put it onto the battlefield, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                                        new PermanentHasSubtypePredicate(CardSubtype.PLAINS))),
                                "Sacrifice a Forest or Plains"),
                        new SearchLibraryEffect(
                                new CardTypePredicate(CardType.LAND), LibrarySearchDestination.BATTLEFIELD)
                ),
                "{T}, Sacrifice a Forest or Plains: Search your library for a land card, put it onto the battlefield, then shuffle."
        ));
    }
}
