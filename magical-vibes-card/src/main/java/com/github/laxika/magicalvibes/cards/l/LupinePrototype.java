package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHandEmpty;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "267")
@CardRegistration(set = "INR", collectorNumber = "444")
public class LupinePrototype extends Card {

    public LupinePrototype() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new AnyOf(List.of(new ControllerHandEmpty(), new AnOpponentHandEmpty())),
                "a player has no cards in hand"
        ));
    }
}
