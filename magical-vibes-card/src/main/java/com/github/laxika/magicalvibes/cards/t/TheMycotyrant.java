package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DescentsThisTurn;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "235")
public class TheMycotyrant extends Card {

    public TheMycotyrant() {
        PermanentCount fungiAndSaprolings = new PermanentCount(
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.FUNGUS, CardSubtype.SAPROLING)),
                CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(fungiAndSaprolings, fungiAndSaprolings));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new CreateTokenEffect(
                CardType.CREATURE,
                new DescentsThisTurn(),
                "Fungus",
                1,
                1,
                CardColor.BLACK,
                null,
                List.of(CardSubtype.FUNGUS),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.STATIC, new CantBlockEffect()),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()
        ));
    }
}
