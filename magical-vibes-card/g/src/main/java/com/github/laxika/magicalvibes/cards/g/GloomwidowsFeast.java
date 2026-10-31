package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

/**
 * Gloomwidow's Feast — {3}{G} Instant.
 * Destroy target creature with flying. If that creature was blue or black, create a 1/2 green
 * Spider creature token with reach.
 */
@CardRegistration(set = "SHM", collectorNumber = "118")
public class GloomwidowsFeast extends Card {

    public GloomwidowsFeast() {
        // The colour check is a last-known-information gate on the destroyed creature, and the token
        // is created only after the destroy has happened, as the printed instructions are ordered.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING))),
                "Target must be a creature with flying."
        ))
                .addSpellEffect(new DestroyTargetPermanentThenEffect(
                        EventStat.NONE,
                        new CreateTokenEffect("Spider", 1, 2, CardColor.GREEN,
                                List.of(CardSubtype.SPIDER), Set.of(Keyword.REACH), Set.<CardType>of()),
                        ThenEffectRecipient.CONTROLLER,
                        new PermanentColorInPredicate(Set.of(CardColor.BLUE, CardColor.BLACK))));
    }
}
