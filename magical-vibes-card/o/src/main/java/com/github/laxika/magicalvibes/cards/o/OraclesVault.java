package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "234")
public class OraclesVault extends Card {

    public OraclesVault() {
        // {2}, {T}: Exile the top card of your library. Until end of turn, you may play that card.
        // Put a brick counter on Oracle's Vault.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new ExileTopCardMayPlayThisTurnEffect(false),
                        new PutCountersOnSelfEffect(CounterType.BRICK)
                ),
                "{2}, {T}: Exile the top card of your library. Until end of turn, you may play that card. Put a brick counter on Oracle's Vault."
        ));

        // {T}: Exile the top card of your library. Until end of turn, you may play that card without
        // paying its mana cost. Activate only if there are three or more brick counters on Oracle's Vault.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{0}",
                List.of(new ExileTopCardMayPlayThisTurnEffect(true)),
                "{T}: Exile the top card of your library. Until end of turn, you may play that card without paying its mana cost. Activate only if there are three or more brick counters on Oracle's Vault."
        ).withRequiredSourceCounters(CounterType.BRICK, 3));
    }
}
