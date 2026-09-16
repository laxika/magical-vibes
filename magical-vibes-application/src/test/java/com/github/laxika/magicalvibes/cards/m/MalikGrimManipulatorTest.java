package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MalikGrimManipulator.class, GrizzlyBears.class, HillGiant.class})
@DisplayName("Malik, Grim Manipulator")
class MalikGrimManipulatorTest extends BaseCardTest {

    @Test
    @DisplayName("Both players choose before the creatures are sacrificed and Malik creates a Treasure")
    void bothPlayersChooseBeforeSacrifice() {
        Permanent ownChosen = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new HillGiant());
        Permanent opponentChosen = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());

        castMalik(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.context())
                .isInstanceOf(MultiPermanentChoiceContext.ControllerAndTargetPlayerChooseCreaturesThenSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(ownChosen.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(ownChosen.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentChosen.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ownChosen.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentChosen.getId()));

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("The enter-the-battlefield ability cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new MalikGrimManipulator()));
        addMalikMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castMalik(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new MalikGrimManipulator()));
        addMalikMana();
        harness.castCreature(player1, 0, targetId);
    }

    private void addMalikMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
