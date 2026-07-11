package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "167")
@CardRegistration(set = "M11", collectorNumber = "163")
public class AwakenerDruid extends Card {

    public AwakenerDruid() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                "Target must be a Forest"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new AnimatePermanentsEffect(
                                4, 5, List.of(CardSubtype.TREEFOLK), Set.of(), CardColor.GREEN, Set.of(),
                                GrantScope.TARGET, EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD));
    }
}
