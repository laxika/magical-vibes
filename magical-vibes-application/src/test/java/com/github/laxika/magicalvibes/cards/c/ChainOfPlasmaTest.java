package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChainOfPlasma.class, GrizzlyBears.class})
class ChainOfPlasmaTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage, then the target player may discard and copy it")
    void targetPlayerMayDiscardAndCopy() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        castAt(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(mayChoicePlayer()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        assertThat(discardChoicePlayer()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(mayChoicePlayer()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A permanent's controller may discard and copy it")
    void targetPermanentControllerMayDiscardAndCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        castAt(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(mayChoicePlayer()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Declining to discard does not create a copy")
    void decliningDiscardDoesNotCopy() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castAt(player2.getId());

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void castAt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ChainOfPlasma()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private java.util.UUID mayChoicePlayer() {
        return gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId();
    }

    private java.util.UUID discardChoicePlayer() {
        return gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId();
    }
}
