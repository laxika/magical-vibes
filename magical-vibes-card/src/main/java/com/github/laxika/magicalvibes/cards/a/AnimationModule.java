package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddAnotherCounterOfChosenTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "194")
public class AnimationModule extends Card {

    public AnimationModule() {
        addEffect(EffectSlot.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_PERMANENT,
                new MayPayManaEffect("{1}",
                        new CreateTokenEffect(1, "Servo", 1, 1, null,
                                List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT)),
                        "Pay {1} to create a 1/1 colorless Servo artifact creature token?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new AddAnotherCounterOfChosenTypeToTargetEffect()),
                "{3}, {T}: Choose a counter on target permanent or player. Give that permanent or player another counter of that kind."
        ));
    }
}
