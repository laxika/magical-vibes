package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RIX", collectorNumber = "157")
public class ElendaTheDuskRose extends Card {

    public ElendaTheDuskRose() {
        // Whenever another creature dies, put a +1/+1 counter on Elenda.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new PutCountersOnSourceEffect(1, 1, 1));

        // When Elenda dies, create X 1/1 white Vampire creature tokens with lifelink, where X is
        // Elenda's power. SourcePower uses Elenda's last-known power after it leaves the battlefield.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                new SourcePower(),
                "Vampire",
                1,
                1,
                CardColor.WHITE,
                List.of(CardSubtype.VAMPIRE),
                Set.of(Keyword.LIFELINK),
                Set.of()
        ));
    }
}
