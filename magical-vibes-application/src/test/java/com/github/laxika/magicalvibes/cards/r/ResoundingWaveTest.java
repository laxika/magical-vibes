package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResoundingWaveTest extends BaseCardTest {

    // ===== Main spell: return target permanent to owner's hand =====

    @Test
    @DisplayName("Returns the target creature to its owner's hand")
    void returnsTargetCreatureToHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can return a noncreature permanent (a land)")
    void returnsTargetLand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
    }

    // ===== Cycling reflexive trigger =====

    @Test
    @DisplayName("Cycling returns two chosen permanents to owners' hands and draws a card")
    void cyclingReturnsTwoPermanentsAndDraws() {
        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        addCyclingMana(player1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID islandId = harness.getPermanentId(player2, "Island");

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bearId, islandId));

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Island");
        // The cycling draw still happens: Resounding Wave is discarded, the library card drawn.
        harness.assertInGraveyard(player1, "Resounding Wave");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling may return fewer than two — choosing none still draws a card")
    void cyclingMayReturnNone() {
        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        // Nothing bounced, but the cycling draw resolves.
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void addCyclingMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
