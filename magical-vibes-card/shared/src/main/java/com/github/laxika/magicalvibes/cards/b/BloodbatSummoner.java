package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

/**
 * Bloodbat Summoner — back face of Voldaren Bloodcaster.
 */
public class BloodbatSummoner extends Card {

    public BloodbatSummoner() {
        // At the beginning of combat on your turn, up to one target Blood token you control becomes
        // a 2/2 black Bat creature with flying and haste in addition to its other types.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsTokenPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.BLOOD),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                "Target must be a Blood token you control"
        ), 0, 1)
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new AnimatePermanentsEffect(
                        2, 2,
                        List.of(CardSubtype.BAT),
                        Set.of(Keyword.FLYING, Keyword.HASTE),
                        CardColor.BLACK,
                        Set.of(),
                        GrantScope.TARGET,
                        EffectDuration.PERMANENT
                ));
    }
}
