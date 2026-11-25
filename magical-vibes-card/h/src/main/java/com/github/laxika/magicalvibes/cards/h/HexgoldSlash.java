package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "ONE", collectorNumber = "137")
public class HexgoldSlash extends Card {

    public HexgoldSlash() {
        addSpellEffect(new ConditionalReplacementEffect(
                new TargetPermanentMatches(new PermanentHasKeywordPredicate(Keyword.TOXIC)),
                DealDamageEffect.toTargetCreature(2),
                DealDamageEffect.toTargetCreature(4)
        ));
    }
}
