package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "222")
public class TheMightyThorJaneFoster extends Card {

    public TheMightyThorJaneFoster() {
        PermanentPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()
                ))
        ));

        target(new PermanentPredicateTargetFilter(
                targetFilter,
                "Target must be a nontoken artifact or creature"
        ), 0, 1).addEffect(EffectSlot.ON_ATTACK,
                FlickerEffect.flickerTargetReturningTapped());
        addEffect(EffectSlot.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD, new DrawCardEffect());
    }
}
