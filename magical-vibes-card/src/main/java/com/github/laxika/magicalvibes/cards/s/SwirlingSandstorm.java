package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "JUD", collectorNumber = "102")
public class SwirlingSandstorm extends Card {

    public SwirlingSandstorm() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new MassDamageEffect(5, false, false,
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))));
    }
}
