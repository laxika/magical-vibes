package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GabrielAngelfireTest extends BaseCardTest {

    @ParameterizedTest
    @ValueSource(strings = {"Flying", "First strike", "Trample"})
    @DisplayName("Upkeep choice grants the selected keyword until the next upkeep")
    void grantsChosenKeywordUntilNextUpkeep(String choice) {
        Permanent gabriel = addGabriel();

        chooseAtUpkeep(choice);

        Keyword keyword = switch (choice) {
            case "Flying" -> Keyword.FLYING;
            case "First strike" -> Keyword.FIRST_STRIKE;
            case "Trample" -> Keyword.TRAMPLE;
            default -> throw new IllegalArgumentException(choice);
        };
        assertThat(gqs.hasKeyword(gd, gabriel, keyword)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        advanceToUpkeep(player1);

        assertThat(gqs.hasKeyword(gd, gabriel, keyword)).isFalse();
    }

    @Test
    @DisplayName("Rampage 3 gives +3/+3 for each blocker beyond the first")
    void grantsRampageThree() {
        Permanent gabriel = addGabriel();
        chooseAtUpkeep("Rampage 3");
        gabriel.setAttacking(true);
        addReadyBears();
        addReadyBears();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(gabriel.getPowerModifier()).isEqualTo(3);
        assertThat(gabriel.getToughnessModifier()).isEqualTo(3);
    }

    private Permanent addGabriel() {
        Permanent gabriel = harness.addToBattlefieldAndReturn(player1, new GabrielAngelfire());
        gabriel.setSummoningSick(false);
        return gabriel;
    }

    private void chooseAtUpkeep(String choice) {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, choice);
    }

    private void addReadyBears() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);
    }
}
