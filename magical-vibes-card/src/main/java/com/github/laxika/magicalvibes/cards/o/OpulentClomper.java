package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceHasColor;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantRandomColorToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

import java.util.Arrays;

@CardRegistration(set = "YECL", collectorNumber = "15")
public class OpulentClomper extends Card {

    public OpulentClomper() {
        ColorsAmongControlledPermanents colorsAmongControlledPermanents = new ColorsAmongControlledPermanents();
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(colorsAmongControlledPermanents, colorsAmongControlledPermanents));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new NotCondition(new AllOf(Arrays.stream(CardColor.values())
                        .map(color -> (Condition) new SourceHasColor(color))
                        .toList())),
                new GrantRandomColorToSourceEffect()));
    }
}
