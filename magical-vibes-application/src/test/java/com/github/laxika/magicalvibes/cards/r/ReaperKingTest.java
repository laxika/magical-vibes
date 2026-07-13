package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OneEyedScarecrow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReaperKingTest extends BaseCardTest {

    // ===== Static: other Scarecrows you control get +1/+1 =====

    @Test
    @DisplayName("Other Scarecrows you control get +1/+1, but Reaper King itself does not")
    void boostsOtherScarecrows() {
        Permanent reaperKing = harness.addToBattlefieldAndReturn(player1, new ReaperKing());
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new OneEyedScarecrow());

        // One-Eyed Scarecrow is a 2/3 → 3/4 with Reaper King's anthem.
        assertThat(gqs.getEffectivePower(gd, scarecrow)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scarecrow)).isEqualTo(4);

        // Reaper King is a 6/6 and does not boost itself ("Other Scarecrow creatures").
        assertThat(gqs.getEffectivePower(gd, reaperKing)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, reaperKing)).isEqualTo(6);
    }

    // ===== Trigger: another Scarecrow enters → destroy target permanent =====

    @Test
    @DisplayName("A Scarecrow entering queues the destroy trigger for target selection")
    void scarecrowEnterQueuesTargetSelection() {
        harness.addToBattlefieldAndReturn(player1, new ReaperKing());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castScarecrow(player1);
        harness.passBothPriorities(); // resolve the creature spell (Scarecrow enters, trigger fires)

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
    }

    @Test
    @DisplayName("Resolving the trigger destroys the chosen permanent")
    void scarecrowEnterDestroysChosenPermanent() {
        harness.addToBattlefieldAndReturn(player1, new ReaperKing());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castScarecrow(player1);
        harness.passBothPriorities(); // resolve the creature spell → trigger awaits target

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities(); // resolve the destroy ability

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(victim.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A non-Scarecrow entering does not trigger the destroy ability")
    void nonScarecrowEnterDoesNotTrigger() {
        harness.addToBattlefieldAndReturn(player1, new ReaperKing());
        Permanent bystander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        // Cast a Bear (not a Scarecrow) under player1's control.
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bystander.getId()));
    }

    @Test
    @DisplayName("The trigger cannot target a player (destroys a permanent only)")
    void triggerCannotTargetPlayer() {
        harness.addToBattlefieldAndReturn(player1, new ReaperKing());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castScarecrow(player1);
        harness.passBothPriorities(); // resolve the creature spell → trigger awaits target

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castScarecrow(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new OneEyedScarecrow()));
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castCreature(player, 0);
    }
}
