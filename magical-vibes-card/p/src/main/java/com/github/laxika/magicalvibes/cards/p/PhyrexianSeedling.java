package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "343")
@CardRegistration(set = "MB2", collectorNumber = "581")
public class PhyrexianSeedling extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("PhyrexianSeedling", new OracleData(
                "Phyrexian Seedling",
                CardType.CREATURE,
                Set.of(),
                "{2}{G}",
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(),
                List.of(CardSubtype.PHYREXIAN, CardSubtype.PLANT),
                "Phyrexian Seedling enters the battlefield with a +1/+1 counter on it.\n"
                        + "Proliferatelink (Damage dealt by this creature also causes you to proliferate that many times. "
                        + "You proliferate before checking for lethal damage on creatures.)",
                0,
                0,
                Set.of(Keyword.PROLIFERATELINK),
                null,
                null,
                null));
    }

    public PhyrexianSeedling() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));
        addEffect(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE,
                new ProliferateEffect(new EventValue()));
    }
}
