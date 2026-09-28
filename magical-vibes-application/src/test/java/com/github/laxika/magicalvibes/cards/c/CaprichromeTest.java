package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Caprichrome.class, DarksteelRelic.class, GrizzlyBears.class})
class CaprichromeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact gives Caprichrome a +1/+1 counter")
    void sacrificingArtifactAddsCounter() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castCaprichrome();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(relic.getId()));

        assertThat(findPermanent(player1, "Caprichrome")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Darksteel Relic")).isZero();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing no artifacts leaves Caprichrome without counters")
    void choosingNoArtifactsAddsNoCounters() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castCaprichrome();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Caprichrome")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    private void castCaprichrome() {
        harness.setHand(player1, new ArrayList<>(List.of(new Caprichrome())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
    }
}
