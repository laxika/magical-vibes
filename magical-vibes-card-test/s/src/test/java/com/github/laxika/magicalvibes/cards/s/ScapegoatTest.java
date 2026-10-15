package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.cards.v.VenerableMonk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scapegoat.class, HonorGuard.class, VenerableMonk.class})
class ScapegoatTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature as an additional cost and returns all selected creatures")
    void sacrificesAndReturnsSelectedCreatures() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        Permanent targetA = harness.addToBattlefieldAndReturn(player1, new VenerableMonk());
        Permanent targetB = harness.addToBattlefieldAndReturn(player1, new VenerableMonk());
        harness.setHand(player1, List.of(new Scapegoat()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(targetA.getId(), targetB.getId()), List.of(), false, sacrifice.getId(), null,
                null, null, null, false, null, null, null, List.of());

        harness.assertInGraveyard(player1, "Honor Guard");
        harness.assertOnBattlefield(player1, "Venerable Monk");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Venerable Monk");
        harness.assertNotOnBattlefield(player1, "Venerable Monk");
        harness.assertInGraveyard(player1, "Scapegoat");
    }

    @Test
    @DisplayName("Returns only the selected creatures")
    void returnsOnlySelectedCreatures() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        Permanent selected = harness.addToBattlefieldAndReturn(player1, new VenerableMonk());
        harness.addToBattlefieldAndReturn(player1, new VenerableMonk());
        harness.setHand(player1, List.of(new Scapegoat()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantWithSacrifice(player1, 0, selected.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Honor Guard");
        harness.assertInHand(player1, "Venerable Monk");
        harness.assertOnBattlefield(player1, "Venerable Monk");
    }

    @Test
    @DisplayName("Allows zero creature targets")
    void allowsZeroTargets() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        harness.setHand(player1, List.of(new Scapegoat()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Honor Guard");
        harness.assertInGraveyard(player1, "Scapegoat");
    }

    @Test
    @DisplayName("Cannot cast without sacrificing a creature")
    void cannotCastWithoutSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VenerableMonk());
        harness.setHand(player1, List.of(new Scapegoat()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new VenerableMonk());
        harness.setHand(player1, List.of(new Scapegoat()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, opponentCreature.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Can sacrifice its only target, which is not returned")
    void canSacrificeItsOnlyTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        harness.setHand(player1, List.of(new Scapegoat()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantWithSacrifice(player1, 0, sacrifice.getId(), sacrifice.getId());
        harness.assertInGraveyard(player1, "Honor Guard");
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Honor Guard");
        harness.assertInGraveyard(player1, "Scapegoat");
    }

    @Test
    @DisplayName("Returns remaining legal targets when a selected creature pays the sacrifice cost")
    void returnsRemainingTargetsAfterSacrificingSelectedCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new VenerableMonk());
        harness.setHand(player1, List.of(new Scapegoat()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(sacrifice.getId(), remaining.getId()), List.of(), false, sacrifice.getId(), null,
                null, null, null, false, null, null, null, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Honor Guard");
        harness.assertNotInHand(player1, "Honor Guard");
        harness.assertInHand(player1, "Venerable Monk");
        harness.assertNotOnBattlefield(player1, "Venerable Monk");
    }

    @Test
    @DisplayName("Returns a controlled creature to its owner's hand")
    void returnsCreatureToOwnerRatherThanController() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        VenerableMonk borrowed = new VenerableMonk();
        borrowed.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, borrowed);
        harness.setHand(player1, List.of(new Scapegoat()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Venerable Monk");
        harness.assertNotInHand(player1, "Venerable Monk");
        harness.assertNotOnBattlefield(player1, "Venerable Monk");
    }

    @Test
    @DisplayName("Does not return a target that an opponent controls before resolution")
    void rechecksTargetControllerOnResolution() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        Permanent changedController = harness.addToBattlefieldAndReturn(player1, new VenerableMonk());
        harness.setHand(player1, List.of(new Scapegoat()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantWithSacrifice(player1, 0, changedController.getId(), sacrifice.getId());
        gd.playerBattlefields.get(player1.getId()).remove(changedController);
        gd.playerBattlefields.get(player2.getId()).add(changedController);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Venerable Monk");
        harness.assertNotInHand(player1, "Venerable Monk");
        harness.assertNotInHand(player2, "Venerable Monk");
        harness.assertInGraveyard(player1, "Scapegoat");
    }

    @Test
    @DisplayName("Any number of targets includes more than ninety-nine creatures")
    void canReturnOneHundredCreatures() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HonorGuard());
        List<UUID> targets = IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new VenerableMonk()).getId())
                .toList();
        harness.setHand(player1, List.of(new Scapegoat()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, null, null,
                targets, List.of(), false, sacrifice.getId(), null,
                null, null, null, false, null, null, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(100);
        harness.assertNotOnBattlefield(player1, "Venerable Monk");
        harness.assertInGraveyard(player1, "Honor Guard");
        harness.assertInGraveyard(player1, "Scapegoat");
    }
}
