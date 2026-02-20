package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetLandAndDamageControllerEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CryoclasmTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Cryoclasm has correct card properties")
    void hasCorrectProperties() {
        Cryoclasm card = new Cryoclasm();

        assertThat(card.getName()).isEqualTo("Cryoclasm");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{2}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DestroyTargetLandAndDamageControllerEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Cryoclasm puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Cryoclasm");
        assertThat(entry.getTargetPermanentId()).isEqualTo(targetId);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving destroys target Plains and deals 3 damage to its controller")
    void resolvingDestroysTargetPlainsAndDealsDamage() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Plains"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Resolving destroys target Island and deals 3 damage to its controller")
    void resolvingDestroysTargetIslandAndDealsDamage() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Island"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Can target own Plains")
    void canTargetOwnPlains() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player1, "Plains");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Plains"));
        // Damage dealt to self (player1 is the land's controller)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Fizzles if target land is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // No damage dealt because spell fizzled
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a Mountain")
    void cannotTargetMountain() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, mountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Plains or Island");
    }

    @Test
    @DisplayName("Cryoclasm goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cryoclasm"));
    }
}

