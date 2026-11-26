package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "JUD", collectorNumber = "102")
public class SwirlingSandstorm extends Card {

    public SwirlingSandstorm() {
        addSpellEffect(new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                DealDamageEffect.toEachCreature(5,
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))));
    }
}
