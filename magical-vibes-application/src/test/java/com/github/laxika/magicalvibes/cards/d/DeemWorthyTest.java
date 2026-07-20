package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeemWorthyTest extends BaseCardTest {

    // ===== Main spell: 7 damage to target creature =====

    @Test
    @DisplayName("Deals 7 damage to target creature, destroying a 4/4")
    void deals7ToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new DeemWorthy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Serra Angel"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("Main spell cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // a legal creature target exists
        harness.setHand(player1, List.of(new DeemWorthy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Cycling reflexive trigger: may deal 2 damage to target creature, then draw =====

    @Test
    @DisplayName("Cycling deals 2 damage to target creature, destroying a 2/2, and draws a card")
    void cyclingDeals2ToCreatureAndDraws() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeemWorthy()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        addCyclingMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateHandAbility(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // The cycling draw still happens: Deem Worthy discarded, the library card drawn.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Deem Worthy"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("Cycling may be declined: no target deals no damage but still draws")
    void cyclingWithoutTargetStillDraws() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeemWorthy()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Declining the reflexive trigger leaves the creature unharmed.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // The cycling draw still resolves.
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
    }

    private void addCyclingMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.addMana(player, ManaColor.RED, 1);
    }
}
