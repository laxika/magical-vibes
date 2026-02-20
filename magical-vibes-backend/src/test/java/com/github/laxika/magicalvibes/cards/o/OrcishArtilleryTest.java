package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrcishArtilleryTest {

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
    @DisplayName("Orcish Artillery has correct card properties")
    void hasCorrectProperties() {
        OrcishArtillery card = new OrcishArtillery();

        assertThat(card.getName()).isEqualTo("Orcish Artillery");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{R}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(3);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(2);
        assertThat(card.getActivatedAbilities().get(0).getEffects().get(0))
                .isInstanceOf(DealDamageToAnyTargetEffect.class);
        DealDamageToAnyTargetEffect targetEffect =
                (DealDamageToAnyTargetEffect) card.getActivatedAbilities().get(0).getEffects().get(0);
        assertThat(targetEffect.damage()).isEqualTo(2);
        assertThat(card.getActivatedAbilities().get(0).getEffects().get(1))
                .isInstanceOf(DealDamageToControllerEffect.class);
        DealDamageToControllerEffect controllerEffect =
                (DealDamageToControllerEffect) card.getActivatedAbilities().get(0).getEffects().get(1);
        assertThat(controllerEffect.damage()).isEqualTo(3);
    }

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability targeting player puts it on the stack")
    void activatingTargetingPlayerPutsOnStack() {
        addReadyArtillery(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Orcish Artillery");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Activating ability taps Orcish Artillery")
    void activatingTapsArtillery() {
        Permanent artillery = addReadyArtillery(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(artillery.isTapped()).isTrue();
    }

    // ===== Dealing damage to player =====

    @Test
    @DisplayName("Deals 2 damage to target player and 3 damage to controller")
    void deals2DamageToPlayerAnd3ToController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyArtillery(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Can target self — takes both 2 target damage and 3 controller damage")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        addReadyArtillery(player1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 20 - 2 (target damage) - 3 (controller damage) = 15
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    // ===== Dealing damage to creature =====

    @Test
    @DisplayName("Deals 2 damage to target creature, destroying a 1/1, and 3 damage to controller")
    void deals2DamageDestroying1ToughnessAnd3ToController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new LlanowarElves());

        addReadyArtillery(player1);

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature, 2/2 creature is destroyed, and 3 damage to controller")
    void deals2DamageDestroying2ToughnessAnd3ToController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());

        addReadyArtillery(player1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 2 damage to target 1/3 creature, creature survives, controller takes 3 damage")
    void deals2DamageDoesNotKill3Toughness() {
        harness.setLife(player1, 20);
        // Use another Orcish Artillery as a 1/3 target
        Permanent targetArtillery = new Permanent(new OrcishArtillery());
        targetArtillery.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(targetArtillery);

        addReadyArtillery(player1);

        harness.activateAbility(player1, 0, null, targetArtillery.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Orcish Artillery"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent artillery = addReadyArtillery(player1);
        artillery.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        OrcishArtillery card = new OrcishArtillery();
        Permanent artillery = new Permanent(card);
        // summoningSick is true by default
        harness.getGameData().playerBattlefields.get(player1.getId()).add(artillery);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target creature is removed — controller takes no damage")
    void fizzlesIfTargetCreatureRemoved() {
        harness.setLife(player1, 20);
        addReadyArtillery(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // Controller does NOT take damage when ability fizzles
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Helpers =====

    private Permanent addReadyArtillery(Player player) {
        OrcishArtillery card = new OrcishArtillery();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

