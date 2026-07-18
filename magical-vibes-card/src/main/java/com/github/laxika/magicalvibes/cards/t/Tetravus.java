package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CantBeEnchantedByOtherAurasEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTetraviteTokensToPutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersToCreateTetraviteTokensEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "4ED", collectorNumber = "350")
public class Tetravus extends Card {

    public Tetravus() {
        // Flying — loaded from Scryfall.

        // "This creature enters with three +1/+1 counters on it."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3)));

        // 1/1 colorless Tetravite artifact creature token with flying and "This token can't be enchanted."
        CreateTokenEffect tetravite = new CreateTokenEffect(1, "Tetravite", 1, 1, null,
                List.of(), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT),
                Map.of(EffectSlot.STATIC, new CantBeEnchantedByOtherAurasEffect()));

        // "At the beginning of your upkeep, you may remove any number of +1/+1 counters from this
        //  creature. If you do, create that many 1/1 colorless Tetravite artifact creature tokens.
        //  They each have flying and 'This token can't be enchanted.'"
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new RemoveCountersToCreateTetraviteTokensEffect(tetravite));

        // "At the beginning of your upkeep, you may exile any number of tokens created with this
        //  creature. If you do, put that many +1/+1 counters on this creature."
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ExileTetraviteTokensToPutCountersOnSelfEffect());
    }
}
