package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WelcomingVampire.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class WelcomingVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Draws when a creature with power 2 or less enters under your control")
    void drawsWhenSmallCreatureEnters() {
        addVampire();
        seedLibrary(1);

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Does not trigger for larger creatures or creatures entering under an opponent's control")
    void ignoresLargerAndOpposingCreatures() {
        addVampire();
        seedLibrary(1);

        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        addVampire();
        seedLibrary(2);

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(Forest.class);
        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Triggers again on a later turn")
    void triggersAgainOnLaterTurn() {
        addVampire();
        seedLibrary(3);

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(Forest.class);

        advanceTurn();
        advanceTurn();

        harness.forceActivePlayer(player1);
        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(Forest.class);
    }

    private Permanent addVampire() {
        return harness.addToBattlefieldAndReturn(player1, new WelcomingVampire());
    }

    private void seedLibrary(int count) {
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player1.getId()).add(new Forest());
        }
    }

    private void castGrizzlyBears(Player player) {
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
