package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "246")
public class TropicalStorm extends Card {

    public TropicalStorm() {
        addSimultaneousSpellEffects(DealDamageEffect.toEachCreature(new XValue(),
                new PermanentHasKeywordPredicate(Keyword.FLYING)),
                DealDamageEffect.toEachCreature(1, new PermanentColorInPredicate(Set.of(CardColor.BLUE))));
    }
}
