package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlaughterTheStrongTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses creatures to keep in APNAP order")
    void eachPlayerChoosesInApnapOrder() {
        Permanent player1Kept = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player1Sacrificed = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent player2Sacrificed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player2Kept = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player1Kept.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(player2Kept.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Kept).doesNotContain(player1Sacrificed);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Kept).doesNotContain(player2Sacrificed);
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A choice above four total power is rejected and leaves the prompt active")
    void rejectsChoiceAbovePowerLimit() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        cast();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(bears.getId(), hillGiant.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("above 4");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Negative power reduces the total power of the creatures kept")
    void negativePowerReducesTotal() {
        Permanent negativePowerCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        TestCards.mutableCard(negativePowerCreature).setPower(-1);
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        cast();

        harness.handleMultiplePermanentsChosen(player1,
                List.of(negativePowerCreature.getId(), hillGiant.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(negativePowerCreature, hillGiant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void cast() {
        harness.setHand(player1, List.of(new SlaughterTheStrong()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
