package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GabrielAngelfireTest extends BaseCardTest {

    @Test
    @DisplayName("The upkeep choice grants flying until the next upkeep")
    void grantsFlyingUntilNextUpkeep() {
        Permanent gabriel = addGabriel();

        choose("Flying");

        assertThat(gqs.hasKeyword(gd, gabriel, Keyword.FLYING)).isTrue();
        advanceToUpkeep(player2);
        assertThat(gqs.hasKeyword(gd, gabriel, Keyword.FLYING)).isTrue();
        advanceToUpkeep(player1);
        assertThat(gqs.hasKeyword(gd, gabriel, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The upkeep choice grants first strike")
    void grantsFirstStrike() {
        Permanent gabriel = addGabriel();

        choose("First strike");

        assertThat(gqs.hasKeyword(gd, gabriel, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The upkeep choice grants trample")
    void grantsTrample() {
        Permanent gabriel = addGabriel();

        choose("Trample");

        assertThat(gqs.hasKeyword(gd, gabriel, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The upkeep choice grants rampage 3")
    void grantsRampageThree() {
        Permanent gabriel = addGabriel();
        addReadyBears(player2);
        addReadyBears(player2);

        choose("Rampage 3");

        gabriel.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gabriel)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, gabriel)).isEqualTo(7);
    }

    private Permanent addGabriel() {
        return addCreatureReady(player1, new GabrielAngelfire());
    }

    private void choose(String label) {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, label);
    }

    private void addReadyBears(Player player) {
        addCreatureReady(player, new GrizzlyBears());
    }
}
