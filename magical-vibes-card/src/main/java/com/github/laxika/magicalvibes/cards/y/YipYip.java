package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "43")
public class YipYip extends Card {

    public YipYip() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentHasSubtypePredicate(CardSubtype.ALLY)),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)));
    }
}
