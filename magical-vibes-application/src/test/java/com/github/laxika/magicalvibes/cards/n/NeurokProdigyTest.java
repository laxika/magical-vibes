package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NeurokProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding an artifact card puts Neurok Prodigy's return ability on the stack")
    void artifactDiscardCostPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new NeurokProdigy());
        harness.setHand(player1, List.of(new LeoninScimitar()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Discarding an artifact card returns Neurok Prodigy to its owner's hand")
    void artifactDiscardCostReturnsNeurokProdigyToHand() {
        harness.addToBattlefield(player1, new NeurokProdigy());
        harness.setHand(player1, List.of(new LeoninScimitar()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertInHand(player1, "Neurok Prodigy");
        harness.assertNotOnBattlefield(player1, "Neurok Prodigy");
    }

    @Test
    @DisplayName("Neurok Prodigy cannot discard a nonartifact card as its activation cost")
    void cannotDiscardNonartifactCard() {
        harness.addToBattlefield(player1, new NeurokProdigy());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Neurok Prodigy cannot activate without an artifact card in hand")
    void cannotActivateWithoutArtifactCardInHand() {
        harness.addToBattlefield(player1, new NeurokProdigy());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
