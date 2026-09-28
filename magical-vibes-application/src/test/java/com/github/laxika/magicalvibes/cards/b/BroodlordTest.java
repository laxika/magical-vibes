package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Broodlord.class, GrizzlyBears.class})
class BroodlordTest extends BaseCardTest {

    @Test
    @DisplayName("Ravenous enters with X counters and does not draw below X=5")
    void ravenousBelowThreshold() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castBroodlord(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent broodlord = findPermanent(player1, "Broodlord");
        assertThat(broodlord.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ravenous draws a card when X is 5 or more")
    void ravenousDrawsAtThreshold() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castBroodlord(5);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Broodlord")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Brood Telepathy distributes X counters among other creatures you control")
    void distributesCountersAmongOtherOwnCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.pendingETBDamageAssignments = Map.of(first.getId(), 1, second.getId(), 2);

        castBroodlord(3);
        harness.passBothPriorities();
        chooseTargets(first, second);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanent(player1, "Broodlord")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Brood Telepathy cannot target the Broodlord itself or an opponent's creature")
    void onlyOtherOwnCreaturesAreLegalTargets() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBroodlord(2);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        Permanent broodlord = findPermanent(player1, "Broodlord");
        assertThat(choice.validPermanentIds()).contains(ownCreature.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(broodlord.getId(), opponentCreature.getId());
    }

    private void chooseTargets(Permanent... targets) {
        for (Permanent target : targets) {
            harness.handlePermanentChosen(player1, target.getId());
        }
    }

    private void castBroodlord(int x) {
        harness.setHand(player1, List.of(new Broodlord()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x + 3);
        gs.playCard(gd, player1, 0, x, null, null);
    }
}
