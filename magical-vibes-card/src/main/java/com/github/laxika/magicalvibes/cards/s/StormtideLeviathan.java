package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "74")
public class StormtideLeviathan extends Card {

    public StormtideLeviathan() {
        // All lands are Islands in addition to their other types.
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(
                CardSubtype.ISLAND, GrantScope.ALL_PERMANENTS, false, new PermanentIsLandPredicate()
        ));

        // Creatures without flying or islandwalk can't attack.
        addEffect(EffectSlot.STATIC, new CreaturesCantAttackUnlessPredicateEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                        new PermanentHasKeywordPredicate(Keyword.ISLANDWALK)
                ))
        ));
    }
}
