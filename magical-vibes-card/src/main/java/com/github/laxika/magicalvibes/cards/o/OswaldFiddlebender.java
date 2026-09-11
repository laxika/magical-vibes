package com.github.laxika.magicalvibes.cards.o;

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

@CardRegistration(set = "AFR", collectorNumber = "28")
public class OswaldFiddlebender extends Card {

    public OswaldFiddlebender() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsArtifactPredicate(), "an artifact", false, false, true, false),
                        new SearchLibraryEffect(
                                new CardTypePredicate(CardType.ARTIFACT),
                                LibrarySearchDestination.BATTLEFIELD,
                                new ManaValueBound(true, 1))
                ),
                "{W}, {T}, Sacrifice an artifact: Search your library for an artifact card with mana value equal "
                        + "to 1 plus the sacrificed artifact's mana value, put it onto the battlefield, then shuffle. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
