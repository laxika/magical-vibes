package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WAR", collectorNumber = "44")
public class CallousDismissal extends Card {

    public CallousDismissal() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());

        addEffect(EffectSlot.SPELL, new AmassGoblinsEffect(1, CardSubtype.ZOMBIE));
    }
}
