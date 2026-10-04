package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CircuitMender;
import com.github.laxika.magicalvibes.cards.c.CoilingStalker;
import com.github.laxika.magicalvibes.cards.n.NetworkTerminal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnthusiasticMechanaut.class, CircuitMender.class, CoilingStalker.class, NetworkTerminal.class})
class EnthusiasticMechanautTest extends BaseCardTest {

    @Test
    void reducesArtifactSpellCosts() {
        harness.addToBattlefield(player1, new EnthusiasticMechanaut());
        harness.setHand(player1, List.of(new CircuitMender()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceNonartifactSpellCosts() {
        harness.addToBattlefield(player1, new EnthusiasticMechanaut());
        harness.setHand(player1, List.of(new CoilingStalker()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyReducesArtifactSpellsCastByController() {
        harness.addToBattlefield(player1, new EnthusiasticMechanaut());
        harness.setHand(player2, List.of(new CircuitMender()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castArtifact(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleMechanautsStackTheirReductions() {
        harness.addToBattlefield(player1, new EnthusiasticMechanaut());
        harness.addToBattlefield(player1, new EnthusiasticMechanaut());
        harness.setHand(player1, List.of(new CircuitMender()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reductionsCanMakeAnArtifactSpellFree() {
        harness.addToBattlefield(player1, new EnthusiasticMechanaut());
        harness.addToBattlefield(player1, new EnthusiasticMechanaut());
        harness.addToBattlefield(player1, new EnthusiasticMechanaut());
        harness.addToBattlefield(player1, new EnthusiasticMechanaut());
        harness.setHand(player1, List.of(new CircuitMender()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceColoredManaRequirements() {
        harness.addToBattlefield(player1, new EnthusiasticMechanaut());
        harness.setHand(player1, List.of(new EnthusiasticMechanaut()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mechanautInHandDoesNotReduceArtifactSpellCosts() {
        harness.setHand(player1, List.of(new CircuitMender(), new EnthusiasticMechanaut()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesNoncreatureArtifactSpellCosts() {
        harness.addToBattlefield(player1, new EnthusiasticMechanaut());
        harness.setHand(player1, List.of(new NetworkTerminal()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
