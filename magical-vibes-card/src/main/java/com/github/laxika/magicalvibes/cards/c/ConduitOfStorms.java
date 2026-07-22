package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaAtNextMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "150")
public class ConduitOfStorms extends Card {

    public ConduitOfStorms() {
        ConduitOfEmrakul backFace = new ConduitOfEmrakul();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // Whenever this creature attacks, add {R} at the beginning of your next main phase this turn.
        addEffect(EffectSlot.ON_ATTACK,
                new RegisterDelayedManaAtNextMainPhaseEffect(ManaColor.RED, 1));

        // {3}{R}{R}: Transform this creature.
        addActivatedAbility(new ActivatedAbility(
                false, "{3}{R}{R}",
                List.of(new TransformSelfEffect()),
                "{3}{R}{R}: Transform this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ConduitOfEmrakul";
    }
}
