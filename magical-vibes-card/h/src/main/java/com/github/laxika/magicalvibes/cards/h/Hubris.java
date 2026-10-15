package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetAndAttachedMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "41")
public class Hubris extends Card {

    public Hubris() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ReturnTargetAndAttachedMatchingToHandEffect(
                new PermanentHasSubtypePredicate(CardSubtype.AURA)));
    }
}
