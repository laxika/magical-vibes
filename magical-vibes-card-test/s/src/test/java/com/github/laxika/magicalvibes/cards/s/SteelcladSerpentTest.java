package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.ObeliskOfEsper;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteelcladSerpent.class, ObeliskOfEsper.class})
class SteelcladSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack when controller controls another artifact")
    void canAttackWithAnotherArtifact() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SteelcladSerpent());
        harness.addToBattlefield(player1, new ObeliskOfEsper());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot attack when the only artifact is itself")
    void cannotAttackWhenOnlyArtifactIsItself() {
        addCreatureReady(player1, new SteelcladSerpent());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only opponent controls another artifact")
    void cannotAttackWhenOnlyOpponentControlsArtifact() {
        addCreatureReady(player1, new SteelcladSerpent());
        harness.addToBattlefield(player2, new ObeliskOfEsper());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped artifact still allows the Serpent to attack")
    void canAttackWithTappedArtifact() {
        addCreatureReady(player1, new SteelcladSerpent());
        harness.addToBattlefieldAndReturn(player1, new ObeliskOfEsper()).tap();

        declareAttackers(player1, List.of(0));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Two Serpents can satisfy each other's attack restriction")
    void canAttackWithAnotherSerpent() {
        addCreatureReady(player1, new SteelcladSerpent());
        addCreatureReady(player1, new SteelcladSerpent());

        declareAttackers(player1, List.of(0, 1));

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("The Serpent can block without another artifact")
    void canBlockWithoutAnotherArtifact() {
        addCreatureReady(player1, new SteelcladSerpent());
        harness.addToBattlefield(player1, new ObeliskOfEsper());
        addCreatureReady(player2, new SteelcladSerpent());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Steelclad Serpent");
    }
}
