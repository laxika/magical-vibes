package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
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
        // The "was blue or black" check reads the target's colour, so it runs while the creature is
        // still on the battlefield — before the destroy. Colour doesn't change when it's destroyed,
        // so this matches the printed "was blue or black" wording.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING))),
                "Target must be a creature with flying."
        ))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new TargetPermanentMatches(
                                new PermanentColorInPredicate(Set.of(CardColor.BLUE, CardColor.BLACK))),
                        new CreateTokenEffect("Spider", 1, 2, CardColor.GREEN,
                                List.of(CardSubtype.SPIDER), Set.of(Keyword.REACH), Set.<CardType>of())))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
