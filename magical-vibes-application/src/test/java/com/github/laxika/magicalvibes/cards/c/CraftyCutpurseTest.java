package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AnointedProcession;
import com.github.laxika.magicalvibes.cards.g.GatherTheTownsfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CraftyCutpurseTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent-created creature tokens enter under Crafty Cutpurse's controller")
    void redirectsOpponentTokens() {
        resolveCraftyCutpurse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GatherTheTownsfolk()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(tokenCount(player1, "Human")).isEqualTo(2);
        assertThat(tokenCount(player2, "Human")).isZero();
    }

    @Test
    @DisplayName("Crafty Cutpurse is applied before an opponent's token multiplier")
    void redirectsBeforeOpponentTokenMultiplier() {
        harness.addToBattlefield(player2, new AnointedProcession());
        resolveCraftyCutpurse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GatherTheTownsfolk()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(tokenCount(player1, "Human")).isEqualTo(2);
        assertThat(tokenCount(player2, "Human")).isZero();
    }

    @Test
    @DisplayName("Crafty Cutpurse does not change an opponent's nontoken creature")
    void doesNotRedirectOpponentCreature() {
        resolveCraftyCutpurse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> "Grizzly Bears".equals(permanent.getCard().getName()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> "Grizzly Bears".equals(permanent.getCard().getName()));
    }

    @Test
    @DisplayName("Crafty Cutpurse's replacement expires at turn cleanup")
    void expiresAtTurnCleanup() {
        resolveCraftyCutpurse();
        advanceToNextTurn(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GatherTheTownsfolk()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(tokenCount(player1, "Human")).isZero();
        assertThat(tokenCount(player2, "Human")).isEqualTo(2);
    }

    private void resolveCraftyCutpurse() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CraftyCutpurse()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private long tokenCount(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .count();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
