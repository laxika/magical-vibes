package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BomatBazaarBarge;
import com.github.laxika.magicalvibes.cards.j.JhoirasFamiliar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VerdurousGearhulk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoundryInspector.class, JhoirasFamiliar.class, GrizzlyBears.class,
        BomatBazaarBarge.class, VerdurousGearhulk.class})
class FoundryInspectorTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact spells you cast cost {1} less")
    void artifactSpellsCostOneLess() {
        harness.addToBattlefield(player1, new FoundryInspector());
        harness.setHand(player1, List.of(new JhoirasFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Jhoira's Familiar"));
    }

    @Test
    @DisplayName("Nonartifact spells are not reduced")
    void nonartifactSpellsNotReduced() {
        harness.addToBattlefield(player1, new FoundryInspector());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentArtifactSpellsNotReduced() {
        harness.addToBattlefield(player1, new FoundryInspector());
        harness.setHand(player2, List.of(new JhoirasFamiliar()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castArtifact(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncreature artifact spells also cost one less")
    void noncreatureArtifactSpellsCostOneLess() {
        harness.addToBattlefield(player1, new FoundryInspector());
        harness.setHand(player1, List.of(new BomatBazaarBarge()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BomatBazaarBarge.class);
    }

    @Test
    @DisplayName("Multiple Inspectors reduce the cost cumulatively")
    void multipleInspectorsStack() {
        harness.addToBattlefield(player1, new FoundryInspector());
        harness.addToBattlefield(player1, new FoundryInspector());
        harness.setHand(player1, List.of(new FoundryInspector()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        harness.assertNotInHand(player1, "Foundry Inspector");
    }

    @Test
    @DisplayName("Reductions exceeding the generic cost allow casting without mana")
    void reductionCannotMakeCostNegative() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new FoundryInspector());
        }
        harness.setHand(player1, List.of(new FoundryInspector()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(5);
        harness.assertNotInHand(player1, "Foundry Inspector");
    }

    @Test
    @DisplayName("An Inspector being cast does not reduce its own cost")
    void inspectorDoesNotReduceItsOwnCost() {
        harness.setHand(player1, List.of(new FoundryInspector()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Foundry Inspector");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Generic reductions cannot pay colored artifact mana costs")
    void coloredManaCostsAreNotReduced() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new FoundryInspector());
        }
        harness.setHand(player1, List.of(new VerdurousGearhulk()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Verdurous Gearhulk");
        assertThat(gd.stack).isEmpty();
    }
}
