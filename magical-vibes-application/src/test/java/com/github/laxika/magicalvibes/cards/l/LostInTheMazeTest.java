package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LostInTheMaze.class, GrizzlyBears.class, Island.class})
class LostInTheMazeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps exactly X creatures and stuns only creatures you do not control")
    void etbTapsXCreaturesAndStunsOpponents() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        castLostInTheMaze(2);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(ownTarget.getId(), opponentTarget.getId(), untargeted.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(island.getId());

        harness.handlePermanentChosen(player1, ownTarget.getId());
        harness.handlePermanentChosen(player1, opponentTarget.getId());
        harness.passBothPriorities();

        assertThat(ownTarget.isTapped()).isTrue();
        assertThat(opponentTarget.isTapped()).isTrue();
        assertThat(untargeted.isTapped()).isFalse();
        assertThat(ownTarget.getCounterCount(CounterType.STUN)).isZero();
        assertThat(opponentTarget.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapped creatures you control have hexproof")
    void tappedOwnCreaturesHaveHexproof() {
        harness.addToBattlefield(player1, new LostInTheMaze());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        ownCreature.tap();
        opponentCreature.tap();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HEXPROOF)).isFalse();

        ownCreature.untap();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("X=0 does not require ETB targets")
    void zeroXDoesNotRequireTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LostInTheMaze()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castLostInTheMaze(int xValue) {
        harness.setHand(player1, List.of(new LostInTheMaze()));
        harness.addMana(player1, ManaColor.BLUE, xValue + 2);
        gs.playCard(gd, player1, 0, xValue, null, null);
    }
}
