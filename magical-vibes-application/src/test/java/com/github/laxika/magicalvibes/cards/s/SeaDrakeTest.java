package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeaDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns two target lands you control to hand")
    void bouncesTwoOwnLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        List<UUID> landIds = gd.playerBattlefields.get(player1.getId()).stream()
                .map(p -> p.getId()).toList();
        castSeaDrake(landIds);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island"));
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Island")).count()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sea Drake"));
    }

    @Test
    @DisplayName("Cannot target lands you do not control")
    void cannotTargetOpponentLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        UUID ownLand = harness.getPermanentId(player1, "Island");
        UUID opponentLand = harness.getPermanentId(player2, "Island");

        assertThatThrownBy(() -> castSeaDrake(List.of(ownLand, opponentLand)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land you control");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID land = harness.getPermanentId(player1, "Island");
        UUID creature = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> castSeaDrake(List.of(land, creature)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land you control");
    }

    private void castSeaDrake(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new SeaDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, targetIds);
    }
}
