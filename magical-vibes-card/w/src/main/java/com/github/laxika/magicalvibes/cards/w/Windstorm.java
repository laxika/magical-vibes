package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "M10", collectorNumber = "205")
@CardRegistration(set = "M14", collectorNumber = "201")
@CardRegistration(set = "KTK", collectorNumber = "157")
@CardRegistration(set = "DDD", collectorNumber = "25")
@CardRegistration(set = "GVL", collectorNumber = "25")
public class Windstorm extends Card {

    public Windstorm() {
        addSpellEffect(DealDamageEffect.toEachCreature(new XValue(), new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
