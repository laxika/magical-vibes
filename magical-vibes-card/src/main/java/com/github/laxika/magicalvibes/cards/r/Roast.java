package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

@CardRegistration(set = "DTK", collectorNumber = "151")
public class Roast extends Card {

    public Roast() {
        PermanentPredicate withoutFlying = new PermanentNotPredicate(
                new PermanentHasKeywordPredicate(Keyword.FLYING));
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(5, withoutFlying));
    }
}
