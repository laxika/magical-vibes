package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GorillaWarCry.class, GorillaShaman.class})
class GorillaWarCryTest extends BaseCardTest {

    @Test
    @DisplayName("Every creature on the battlefield gains menace until end of turn")
    void allExistingCreaturesGainMenace() {
        harness.forceActivePlayer(player2);
        Permanent mine = addCreatureReady(player1, new GorillaShaman());
        Permanent theirs = addCreatureReady(player2, new GorillaShaman());
        harness.setHand(player1, List.of(new GorillaWarCry()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.hasKeyword(gd, mine, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.MENACE)).isTrue();

        Permanent enteredLater = addCreatureReady(player1, new GorillaShaman());
        assertThat(gqs.hasKeyword(gd, enteredLater, Keyword.MENACE)).isFalse();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, mine, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("A single blocker cannot block an attacker that gained menace")
    void singleBlockerIsIllegal() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new GorillaShaman());
        addCreatureReady(player2, new GorillaShaman());
        harness.setHand(player1, List.of(new GorillaWarCry()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two blockers can still block the menacing attacker")
    void twoBlockersAreLegal() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new GorillaShaman());
        addCreatureReady(player2, new GorillaShaman());
        addCreatureReady(player2, new GorillaShaman());
        harness.setHand(player1, List.of(new GorillaWarCry()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.combatBlockOpponentIdsThisTurn.get(attacker.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        harness.forceActivePlayer(player1);
        addCreatureReady(player1, new GorillaShaman());
        harness.setHand(player1, List.of(new GorillaWarCry()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast once blockers are declared")
    void cannotCastDuringDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new GorillaWarCry()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside combat")
    void cannotCastOutsideCombat() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new GorillaWarCry()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
