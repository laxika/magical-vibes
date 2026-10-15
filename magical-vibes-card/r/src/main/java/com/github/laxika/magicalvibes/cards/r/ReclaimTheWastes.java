package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "ZNR", collectorNumber = "200")
public class ReclaimTheWastes extends Card {

    public ReclaimTheWastes() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{3}"));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Kicked(),
                new SearchLibraryEffect(CardPredicateUtils.basicLand()),
                new SearchLibraryEffect(new Fixed(2), CardPredicateUtils.basicLand(),
                        LibrarySearchDestination.HAND)));
    }
}
