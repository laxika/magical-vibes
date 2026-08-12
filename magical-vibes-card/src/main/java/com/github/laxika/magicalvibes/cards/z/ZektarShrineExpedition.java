package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "155")
public class ZektarShrineExpedition extends Card {

    public ZektarShrineExpedition() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new MayEffect(new PutCountersOnSelfEffect(CounterType.QUEST),
                        "Put a quest counter on Zektar Shrine Expedition?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.QUEST),
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(
                                CardType.CREATURE, 1, "Elemental", 7, 1,
                                CardColor.RED, null, List.of(CardSubtype.ELEMENTAL),
                                Set.of(Keyword.TRAMPLE, Keyword.HASTE), Set.of(),
                                false, false, Map.of(), List.of(),
                                false, true, false, 0, Set.of())
                ),
                "Remove three quest counters from Zektar Shrine Expedition and sacrifice it: "
                        + "Create a 7/1 red Elemental creature token with trample and haste. "
                        + "Exile it at the beginning of the next end step."
        ));
    }
}
