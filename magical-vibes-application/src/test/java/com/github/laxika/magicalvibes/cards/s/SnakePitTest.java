package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SnakePitTest extends BaseCardTest {

    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new SnakePit());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Opponent's blue spell creates a Snake when accepted")
    void opponentBlueSpellCreatesSnake() {
        setUpOpponentTurn();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent snake = findPermanent(player1, "Snake");
        assertThat(snake).isNotNull();
        assertThat(gqs.getEffectivePower(gd, snake)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, snake)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's black spell creates a Snake when accepted")
    void opponentBlackSpellCreatesSnake() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0, (UUID) null);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Snake")).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's non-blue, non-black spell does not trigger")
    void opponentOtherColorSpellDoesNotTrigger() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(countPermanents(player1, "Snake")).isZero();
    }

    @Test
    @DisplayName("Declining the trigger creates no Snake")
    void decliningCreatesNoSnake() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0, (UUID) null);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Snake")).isZero();
    }
}
