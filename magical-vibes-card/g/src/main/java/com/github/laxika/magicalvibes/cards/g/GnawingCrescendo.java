package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "131")
public class GnawingCrescendo extends Card {

    public GnawingCrescendo() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(2, 0));
        addEffect(EffectSlot.SPELL, new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                ratToken()));
    }

    private static CreateTokenEffect ratToken() {
        return new CreateTokenEffect(
                1,
                "Rat",
                1,
                1,
                CardColor.BLACK,
                List.of(CardSubtype.RAT),
                Set.of(),
                Set.of(),
                Map.of(EffectSlot.STATIC, new CantBlockEffect()));
    }
}
