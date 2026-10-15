package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "ONE", collectorNumber = "137")
public class HexgoldSlash extends Card {

    public HexgoldSlash() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new TargetPermanentMatches(new PermanentHasKeywordPredicate(Keyword.TOXIC)),
                new DealDamageToTargetCreatureEffect(2),
                new DealDamageToTargetCreatureEffect(4)
        ));
    }
}
