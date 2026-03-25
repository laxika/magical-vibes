package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Spires of Orazca — back face of Thaumatic Compass.
 * Land.
 * (Transforms from Thaumatic Compass.)
 * {T}: Add {C}.
 * {T}: Untap target attacking creature an opponent controls and remove it from combat.
 */
public class SpiresOfOrazca extends Card {

    public SpiresOfOrazca() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // {T}: Untap target attacking creature an opponent controls and remove it from combat.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new UntapTargetPermanentEffect(), new RemoveTargetFromCombatEffect()),
                "{T}: Untap target attacking creature an opponent controls and remove it from combat.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsAttackingPredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                        )),
                        "Target must be an attacking creature an opponent controls."
                )
        ));
    }
}
