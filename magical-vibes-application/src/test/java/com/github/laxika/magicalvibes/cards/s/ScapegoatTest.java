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
}
