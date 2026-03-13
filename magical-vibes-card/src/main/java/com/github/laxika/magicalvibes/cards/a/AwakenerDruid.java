package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimateTargetLandWhileSourceOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "167")
@CardRegistration(set = "M11", collectorNumber = "163")
public class AwakenerDruid extends Card {

    public AwakenerDruid() {
        setTargetFilter(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                "Target must be a Forest"
        ));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new AnimateTargetLandWhileSourceOnBattlefieldEffect(
                        4, 5, CardColor.GREEN, List.of(CardSubtype.TREEFOLK)));
    }
}
