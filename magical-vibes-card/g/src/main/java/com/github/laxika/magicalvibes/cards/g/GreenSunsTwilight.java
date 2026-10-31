package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsRevealTwoTypesToHandThenRestEffect;

@CardRegistration(set = "ONE", collectorNumber = "169")
public class GreenSunsTwilight extends Card {

    public GreenSunsTwilight() {
        DynamicAmount lookCount = new Sum(new XValue(), new Fixed(1));

        addSpellEffect(new ConditionalEffect(
                new NotCondition(new SpellXAtLeast(5)),
                LookAtTopCardsRevealTwoTypesToHandThenRestEffect
                        .creatureAndLandRequiredRestOnBottomRandom(lookCount, false)));
        addSpellEffect(new ConditionalEffect(
                new SpellXAtLeast(5),
                LookAtTopCardsRevealTwoTypesToHandThenRestEffect
                        .creatureAndLandRequiredRestOnBottomRandom(lookCount, true)));
    }
}
