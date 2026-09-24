package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantEncoreToCreatureCardsOfSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleForControlledSubtypeEffect;

@CardRegistration(set = "CMM", collectorNumber = "707")
@CardRegistration(set = "CMM", collectorNumber = "777")
@CardRegistration(set = "CMM", collectorNumber = "782")
public class SliverGravemother extends Card {

    public SliverGravemother() {
        addEffect(EffectSlot.STATIC, new IgnoreLegendRuleForControlledSubtypeEffect(CardSubtype.SLIVER));
        addEffect(EffectSlot.STATIC, new GrantEncoreToCreatureCardsOfSubtypeEffect(CardSubtype.SLIVER));
    }
}
