package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NetherbornAltar.class, EdgarMarkov.class})
class NetherbornAltarTest extends BaseCardTest {

    @Test
    void returnsCommanderAndLosesLifeForEachSoulCounter() {
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new NetherbornAltar());
        Card commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CommanderReplacementChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(altar.getCounterCount(CounterType.SOUL)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(commander);
        assertThat(gd.playerCommandZones.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 17);

        altar.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(altar.getCounterCount(CounterType.SOUL)).isEqualTo(2);
        harness.assertLife(player1, 11);
    }
}
