package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C14", collectorNumber = "30")
public class SpoilsOfBlood extends Card {

    public SpoilsOfBlood() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Horror",
                new CreatureDeathsThisTurn(CountScope.ANY_PLAYER),
                new CreatureDeathsThisTurn(CountScope.ANY_PLAYER),
                CardColor.BLACK,
                List.of(CardSubtype.HORROR),
                Set.of(),
                Set.of()));
    }
}
