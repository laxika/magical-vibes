package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceBlocksWithAtLeastAndOnlyMatchingBlockers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LEG", collectorNumber = "42")
public class WallOfCaltrops extends Card {

    public WallOfCaltrops() {
        addEffect(EffectSlot.ON_BLOCK, new ConditionalEffect(
                new SourceBlocksWithAtLeastAndOnlyMatchingBlockers(
                        2, new PermanentHasSubtypePredicate(CardSubtype.WALL)),
                new GrantKeywordEffect(Keyword.BANDING, GrantScope.SELF)));
    }
}
