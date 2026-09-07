package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "244")
public class FiftyFeetOfRope extends Card {

    public FiftyFeetOfRope() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                "{T}: Target Wall can't block this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.WALL),
                        "Target must be a Wall")
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new SkipNextUntapEffect(TapUntapScope.TARGET)),
                "{3}, {T}: Target creature doesn't untap during its controller's next untap step.",
                TargetFilters.creature()
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new VentureIntoDungeonEffect()),
                "{4}, {T}: Venture into the dungeon. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
