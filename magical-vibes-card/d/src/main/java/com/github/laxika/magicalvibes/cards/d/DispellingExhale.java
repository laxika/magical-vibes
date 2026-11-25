package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.BeholdCostPaid;
import com.github.laxika.magicalvibes.model.effect.BeholdCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;

@CardRegistration(set = "TDM", collectorNumber = "41")
public class DispellingExhale extends Card {

    public DispellingExhale() {
        addCastingCost(BeholdCost.optional(CardSubtype.DRAGON));
        addSpellEffect(new ConditionalReplacementEffect(
                new BeholdCostPaid(), UnlessPaysEffect.counterUnlessPays(2), UnlessPaysEffect.counterUnlessPays(4)));
    }
}
