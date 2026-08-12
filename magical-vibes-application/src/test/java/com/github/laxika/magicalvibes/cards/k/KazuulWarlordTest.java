package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KazuulWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may put a counter on each Ally you control")
    void ownAllyEntryMayPutCountersOnEachAlly() {
        harness.setHand(player1, List.of(new KazuulWarlord()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent warlord = findPermanent(player1, "Kazuul Warlord");
        assertThat(warlord.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An Ally entry puts counters on every Ally but not non-Allies")
    void anotherAllyEntryPutsCountersOnEveryAlly() {
        Permanent blademaster = harness.addToBattlefieldAndReturn(player1, new KazanduBlademaster());
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KazuulWarlord()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blademaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanent(player1, "Kazuul Warlord")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAlly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter placement may be declined")
    void counterPlacementMayBeDeclined() {
        harness.setHand(player1, List.of(new KazuulWarlord()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Kazuul Warlord")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
