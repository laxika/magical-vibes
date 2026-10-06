package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PatchworkBeastie;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoaringFurnaceSteamingSauna.class, PatchworkBeastie.class})
class RoaringFurnaceSteamingSaunaTest extends BaseCardTest {

    @Test
    void roaringFurnaceDealsDamageEqualToControllerHandSize() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatchworkBeastie());

        harness.setHand(player1, new ArrayList<>(List.of(
                new RoaringFurnaceSteamingSauna(),
                new PatchworkBeastie(), new PatchworkBeastie(), new PatchworkBeastie())));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void roaringFurnaceCannotTargetYourCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new PatchworkBeastie());
        harness.addToBattlefieldAndReturn(player2, new PatchworkBeastie());

        castRoom(0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void steamingSaunaRemovesHandSizeLimitAndDrawsAtYourEndStep() {
        castRoom(1);
        harness.setHand(player1, new ArrayList<>(List.of(
                new PatchworkBeastie(), new PatchworkBeastie(), new PatchworkBeastie(),
                new PatchworkBeastie(), new PatchworkBeastie(), new PatchworkBeastie(),
                new PatchworkBeastie(), new PatchworkBeastie(), new PatchworkBeastie())));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(10);
    }

    @Test
    void lockedSaunaDoesNotDrawAtControllerEndStep() {
        castRoom(0);
        harness.setLibrary(player1, List.of(new PatchworkBeastie()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void lockedSaunaDoesNotRemoveHandSizeLimit() {
        castRoom(0);
        harness.setHand(player1, List.of(
                new PatchworkBeastie(), new PatchworkBeastie(), new PatchworkBeastie(),
                new PatchworkBeastie(), new PatchworkBeastie(), new PatchworkBeastie(),
                new PatchworkBeastie(), new PatchworkBeastie(), new PatchworkBeastie()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
    }

    @Test
    void unlockedSaunaPreventsCleanupDiscard() {
        castRoom(1);
        harness.setHand(player1, List.of(
                new PatchworkBeastie(), new PatchworkBeastie(), new PatchworkBeastie(),
                new PatchworkBeastie(), new PatchworkBeastie(), new PatchworkBeastie(),
                new PatchworkBeastie(), new PatchworkBeastie(), new PatchworkBeastie()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    void saunaDoesNotDrawDuringOpponentEndStep() {
        castRoom(1);
        harness.setLibrary(player1, List.of(new PatchworkBeastie()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void furnaceUsesHandSizeAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatchworkBeastie());
        castRoom(0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player1, List.of(new PatchworkBeastie()));

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void furnaceWithEmptyHandDealsNoDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatchworkBeastie());
        castRoom(0);
        harness.handlePermanentChosen(player1, target.getId());

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void unlockingSaunaAfterCastingFurnaceEnablesEndStepDraw() {
        castRoom(0);
        harness.setLibrary(player1, List.of(new PatchworkBeastie()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.unlockRoomDoor(player1, 0, 1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void unlockingFurnaceAfterCastingSaunaDealsDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatchworkBeastie());
        castRoom(1);
        harness.setHand(player1, List.of(new PatchworkBeastie(), new PatchworkBeastie()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.unlockRoomDoor(player1, 0, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new RoaringFurnaceSteamingSauna()));
        harness.addMana(player1, doorIndex == 0 ? ManaColor.RED : ManaColor.BLUE,
                doorIndex == 0 ? 2 : 5);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROOM))
                .findFirst()
                .orElseThrow();
    }
}
