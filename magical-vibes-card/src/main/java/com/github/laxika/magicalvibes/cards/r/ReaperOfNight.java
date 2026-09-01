package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HarvestFear;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "ELD", collectorNumber = "102")
public class ReaperOfNight extends Card {

    public ReaperOfNight() {
        setBackFaceCard(new HarvestFear());
        addCastingOption(new AdventureCast("{3}{B}"));
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new DefendingPlayerHandAtMost(2),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "HarvestFear";
    }
}
