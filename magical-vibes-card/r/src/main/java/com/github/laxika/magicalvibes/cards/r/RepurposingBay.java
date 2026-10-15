package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "56")
public class RepurposingBay extends Card {

    public RepurposingBay() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsArtifactPredicate(), "another artifact", true, false, true, false),
                        new SearchLibraryEffect(
                                new CardTypePredicate(CardType.ARTIFACT),
                                LibrarySearchDestination.BATTLEFIELD,
                                new ManaValueBound(true, 1))
                ),
                "{2}, {T}, Sacrifice another artifact: Search your library for an artifact card with mana value "
                        + "equal to 1 plus the sacrificed artifact's mana value, put that card onto the battlefield, "
                        + "then shuffle. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
