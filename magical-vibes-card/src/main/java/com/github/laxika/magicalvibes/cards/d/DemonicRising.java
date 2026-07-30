package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AVR", collectorNumber = "94")
public class DemonicRising extends Card {

    public DemonicRising() {
        // At the beginning of your end step, if you control exactly one creature, create a 5/5
        // black Demon creature token with flying. "Exactly one" is the conjunction of the
        // at-least-one and at-most-one creature counts.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new AllOf(List.of(
                                new ControlsPermanentCount(1, new PermanentIsCreaturePredicate()),
                                new ControlsPermanentCountAtMost(1, new PermanentIsCreaturePredicate()))),
                        new CreateTokenEffect("Demon", 5, 5, CardColor.BLACK,
                                List.of(CardSubtype.DEMON), Set.of(Keyword.FLYING), Set.of())));
    }
}
