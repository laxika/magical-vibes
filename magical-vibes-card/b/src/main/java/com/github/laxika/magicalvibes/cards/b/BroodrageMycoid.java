package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DescendedThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "95")
public class BroodrageMycoid extends Card {

    public BroodrageMycoid() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new DescendedThisTurn(),
                new CreateTokenEffect(
                        1,
                        "Fungus",
                        1,
                        1,
                        CardColor.BLACK,
                        List.of(CardSubtype.FUNGUS),
                        Set.of(),
                        Set.of(),
                        Map.of(EffectSlot.STATIC, new CantBlockEffect()))));
    }
}
