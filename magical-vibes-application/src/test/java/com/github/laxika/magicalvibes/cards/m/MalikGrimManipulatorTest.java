package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MalikGrimManipulator.class, GrizzlyBears.class})
class MalikGrimManipulatorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB has the controller and target opponent choose creatures to sacrifice together")
    void eachChosenCreatureIsSacrificedTogether() {
        Permanent ownChoice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownOther = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentChoice = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentOther = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMalik();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(ownChoice.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentChoice.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownOther)
                .doesNotContain(ownChoice);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentOther)
                .doesNotContain(opponentChoice);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Malik's trigger creates no Treasure for a creature sacrificed by its controller")
    void ownSacrificeDoesNotCreateTreasure() {
        Permanent ownChoice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castMalik();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownChoice.getId()));
        harness.handleMultiplePermanentsChosen(player2,
                List.of(gd.playerBattlefields.get(player2.getId()).getFirst().getId()));

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("ETB cannot target its controller")
    void etbRequiresOpponentTarget() {
        harness.setHand(player1, List.of(new MalikGrimManipulator()));
        addManaForMalik();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMalik() {
        harness.setHand(player1, List.of(new MalikGrimManipulator()));
        addManaForMalik();
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addManaForMalik() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
