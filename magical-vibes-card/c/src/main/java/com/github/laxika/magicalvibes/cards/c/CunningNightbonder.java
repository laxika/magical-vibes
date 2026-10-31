package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;

@CardRegistration(set = "IKO", collectorNumber = "219")
public class CunningNightbonder extends Card {

    public CunningNightbonder() {
        CardKeywordPredicate flash = new CardKeywordPredicate(Keyword.FLASH);
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(flash, 1, CostModificationScope.SELF));
        addStaticEffect(new ControllerSpellsCantBeCounteredEffect(flash, true));
    }
}
