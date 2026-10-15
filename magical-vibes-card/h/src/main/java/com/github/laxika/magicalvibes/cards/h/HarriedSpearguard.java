package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "135")
public class HarriedSpearguard extends Card {

    public HarriedSpearguard() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Rat",
                1,
                1,
                CardColor.BLACK,
                null,
                List.of(CardSubtype.RAT),
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
                Set.of()));
    }
}
