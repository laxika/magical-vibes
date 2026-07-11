package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "54")
public class WeatheredWayfarer extends Card {

    public WeatheredWayfarer() {
        // {W}, {T}: Search your library for a land card, reveal it, put it into your hand,
        // then shuffle. Activate only if an opponent controls more lands than you.
        addActivatedAbility(new ActivatedAbility(
                true, "{W}",
                List.of(new SearchLibraryEffect(new CardTypePredicate(CardType.LAND))),
                "{W}, {T}: Search your library for a land card, reveal it, put it into your hand, then shuffle. Activate only if an opponent controls more lands than you.",
                ActivationTimingRestriction.OPPONENT_CONTROLS_MORE_LANDS
        ));
    }
}
