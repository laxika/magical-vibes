package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "379")
@CardRegistration(set = "MB2", collectorNumber = "616")
public class OmenpathToNaya extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("OmenpathToNaya", new OracleData(
                "Omenpath to Naya",
                CardType.LAND,
                Set.of(),
                null,
                null,
                List.of(),
                List.of(CardColor.GREEN, CardColor.RED, CardColor.WHITE),
                Set.of(),
                List.of(CardSubtype.OMEN),
                "Vanishing 4 (This land enters the battlefield with four time counters on it. "
                        + "At the beginning of your upkeep, remove a time counter from it. When the last is removed, "
                        + "sacrifice it.)\n"
                        + "{T}: Add {R}, {G}, or {W}.",
                null,
                null,
                Set.of(Keyword.VANISHING),
                null,
                null,
                null));
    }

    public OmenpathToNaya() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.TIME, new Fixed(4)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(
                        ManaColor.RED, ManaColor.GREEN, ManaColor.WHITE))),
                "{T}: Add {R}, {G}, or {W}."));
    }
}
