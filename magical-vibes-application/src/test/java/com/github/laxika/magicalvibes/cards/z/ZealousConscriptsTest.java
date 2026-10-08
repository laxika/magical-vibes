package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PainSeer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZealousConscripts.class, GrizzlyBears.class, Mountain.class, PainSeer.class})
class ZealousConscriptsTest extends BaseCardTest {

    private void castZealousConscripts(UUID targetId) {
        harness.setHand(player1, List.of(new ZealousConscripts()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }

    @Test
    @DisplayName("ETB trigger goes on the stack targeting the permanent")
    void etbTriggersOnStack() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        castZealousConscripts(targetId);

        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Zealous Conscripts");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Untaps, steals until end of turn and grants haste")
    void stealsUntapsAndGrantsHaste() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        castZealousConscripts(target.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Can steal a land")
    void canStealLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        land.tap();
        castZealousConscripts(land.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(land.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(land.getId())).isTrue();
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castZealousConscripts(target.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Can target your own permanent to untap it and grant haste")
    void canTargetOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.tap();
        castZealousConscripts(target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger resolves even if Zealous Conscripts leaves the battlefield")
    void sourceLeavingDoesNotStopTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        castZealousConscripts(target.getId());
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof ZealousConscripts).findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Zealous Conscripts");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The trigger cannot affect a target that left and returned as a new permanent")
    void targetLeavingAndReturningInvalidatesTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castZealousConscripts(target.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.setHand(player2, List.of());
        Permanent returned = harness.addToBattlefieldAndReturn(player2, target.getCard());
        returned.tap();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Gaining control precedes untapping, so the new controller controls inspired")
    void newControllerControlsUntapTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PainSeer());
        target.tap();
        Mountain topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLibrary(player2, List.of(new Mountain()));
        harness.setHand(player2, List.of());
        castZealousConscripts(target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pain Seer");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
