package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "36")
public class PlatoonDispenser extends Card {

    public PlatoonDispenser() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new ControlsOtherPermanentCount(2, new PermanentIsCreaturePredicate()),
                        new DrawCardEffect(1)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new CreateTokenEffect("Soldier", 1, 1, null,
                        List.of(CardSubtype.SOLDIER), Set.of(), Set.of(CardType.ARTIFACT))),
                "{3}{W}: Create a 1/1 colorless Soldier artifact creature token."
        ));

        addUnearth("{2}{W}{W}");
    }
}
