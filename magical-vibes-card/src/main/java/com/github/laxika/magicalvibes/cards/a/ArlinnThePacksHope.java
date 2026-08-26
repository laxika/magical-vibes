package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeThisTurnEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "211")
public class ArlinnThePacksHope extends Card {

    public ArlinnThePacksHope() {
        setBackFaceCard(new ArlinnTheMoonsFury());

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new GrantFlashToCardTypeThisTurnEffect(CardType.CREATURE,
                                EffectDuration.UNTIL_YOUR_NEXT_TURN),
                        new ControlledCreaturesEnterWithAdditionalCountersThisTurnEffect(
                                1, EffectDuration.UNTIL_YOUR_NEXT_TURN)
                ),
                "+1: Until your next turn, you may cast creature spells as though they had flash, "
                        + "and each creature you control enters with an additional +1/+1 counter on it."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new CreateTokenEffect(2, "Wolf", 2, 2, CardColor.GREEN,
                        List.of(CardSubtype.WOLF), Set.of(), Set.of())),
                "−3: Create two 2/2 green Wolf creature tokens."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ArlinnTheMoonsFury";
    }
}
