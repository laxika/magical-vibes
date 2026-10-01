package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.ExilePermanentCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "141")
@CardRegistration(set = "WHO", collectorNumber = "424")
@CardRegistration(set = "WHO", collectorNumber = "1015")
@CardRegistration(set = "WHO", collectorNumber = "746")
public class LunarHatchling extends Card {

    public LunarHatchling() {
        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "Basic landcycling {2} ({2}, Discard this card: Search your library for a basic land card, "
                        + "reveal it, put it into your hand, then shuffle.)"));

        addCastingOption(new GraveyardCast(null, "{4}{G}{U}", List.of(
                new ExilePermanentCastingCost(new PermanentIsLandPredicate(), "a land"),
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 5)),
                null, false, false, true));
    }
}
