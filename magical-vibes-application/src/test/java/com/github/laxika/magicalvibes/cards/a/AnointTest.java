package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnointTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Anoint targets a creature and goes on the stack")
    void castingPutsItOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(targetId);
        assertThat(entry.isBuyback()).isFalse();
    }

    @Test
    @DisplayName("Resolving Anoint without buyback prevents the next 3 damage to the target")
    void resolvingAddsShield() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(bears(player1).getDamagePreventionShield()).isEqualTo(3);
    }

    @Test
    @DisplayName("Without buyback the spell goes to the graveyard as it resolves")
    void resolvesToGraveyardWithoutBuyback() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(playerHandNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).containsExactly("Anoint");
    }

    @Test
    @DisplayName("Paying buyback returns the spell to hand as it resolves")
    void buybackReturnsToHand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstantWithBuyback(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack.getFirst().isBuyback()).isTrue();

        harness.passBothPriorities();

        assertThat(graveyardNames(player1)).doesNotContain("Anoint");
        assertThat(playerHandNames(player1)).containsExactly("Anoint");
    }

    @Test
    @DisplayName("Buyback spells that fizzle still go to the graveyard")
    void buybackFizzleGoesToGraveyard() {
        // Target leaves the battlefield before Anoint resolves — the spell fizzles and, because
        // it never resolved, buyback does not return it to hand.
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstantWithBuyback(player1, 0, targetId);

        // Opponent destroys the target while Anoint is on the stack.
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(playerHandNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).containsExactly("Anoint");
    }

    @Test
    @DisplayName("Paying buyback with insufficient mana rewinds the cast")
    void buybackWithoutManaRewinds() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstantWithBuyback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        // The failed cast left the card in hand and the mana pool untouched.
        assertThat(playerHandNames(player1)).containsExactly("Anoint");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    @DisplayName("The prevention shield is consumed by incoming damage")
    void shieldPreventsDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Shock deals 2 damage to the shielded creature — fully prevented, 1 shield remains.
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = bears(player1);
        assertThat(bears.getDamagePreventionShield()).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Anoint cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent bears(Player player) {
        return findPermanent(player, "Grizzly Bears");
    }

    private List<String> playerHandNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }
}
