package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "142")
public class AssembleTheLegion extends Card {

    public AssembleTheLegion() {
        // "Then" — the token count reads the muster counter this same trigger just added,
        // so both halves must resolve inside one stack entry.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.MUSTER),
                new CreateTokenEffect(CardType.CREATURE, new CountersOnSource(CounterType.MUSTER),
                        "Soldier", 1, 1, CardColor.RED, Set.of(CardColor.RED, CardColor.WHITE),
                        List.of(CardSubtype.SOLDIER), Set.of(Keyword.HASTE), Set.of(),
                        false, false, Map.of(), List.of(), false, false, false, 0, Set.of())));
    }
}
