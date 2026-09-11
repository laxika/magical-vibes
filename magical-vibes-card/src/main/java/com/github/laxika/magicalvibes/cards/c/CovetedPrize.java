package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.condition.FullParty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

@CardRegistration(set = "ZNR", collectorNumber = "95")
public class CovetedPrize extends Card {

    public CovetedPrize() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new PartySize()));
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new FullParty(),
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(new CardMaxManaValuePredicate(4))));
    }
}
