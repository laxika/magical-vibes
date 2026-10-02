package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SeekHighestManaValueCardEffect;

import java.util.List;

@CardRegistration(set = "YOTJ", collectorNumber = "30")
public class RunecarvedObelisk extends Card {

    public RunecarvedObelisk() {
        // {T}: Add {C}{C}. Put two charge counters on Runecarved Obelisk.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.COLORLESS, 2),
                        new PutCountersOnSelfEffect(CounterType.CHARGE, 2)
                ),
                "{T}: Add {C}{C}. Put two charge counters on Runecarved Obelisk."
        ));

        // {T}, Sacrifice Runecarved Obelisk: Seek a card with the highest mana value among cards
        // in your library with mana value X or less, where X is the number of charge counters on
        // Runecarved Obelisk.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        SacrificeSelfCost.recordingPermanentSnapshot(),
                        new SeekHighestManaValueCardEffect(new CountersOnSource(CounterType.CHARGE))
                ),
                "{T}, Sacrifice Runecarved Obelisk: Seek a card with the highest mana value among "
                        + "cards in your library with mana value X or less, where X is the number "
                        + "of charge counters on Runecarved Obelisk."
        ));
    }
}
