package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "WWK", collectorNumber = "74")
public class ChainReaction extends Card {

    public ChainReaction() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER), false));
    }
}
