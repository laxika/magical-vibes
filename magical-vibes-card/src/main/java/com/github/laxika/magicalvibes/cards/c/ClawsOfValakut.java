package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WWK", collectorNumber = "75")
public class ClawsOfValakut extends Card {

    public ClawsOfValakut() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), CountScope.CONTROLLER),
                        new Fixed(0),
                        GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.ENCHANTED_CREATURE));
    }
}
