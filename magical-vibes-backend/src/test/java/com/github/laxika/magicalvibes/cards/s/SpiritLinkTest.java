package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDamageDealtEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpiritLinkTest {

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
    @DisplayName("Spirit Link has correct card properties")
    void hasCorrectProperties() {
        SpiritLink card = new SpiritLink();

        assertThat(card.getName()).isEqualTo("Spirit Link");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.AURA);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(GainLifeEqualToDamageDealtEffect.class);
    }

    // ===== Unblocked attacker deals damage to player =====

    @Test
    @DisplayName("Controller gains life when enchanted creature deals combat damage to player")
    void controllerGainsLifeOnCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Grizzly Bears (2/2) with Spirit Link attacks unblocked
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        bears.setAttacking(true);
        attachSpiritLink(player1, bears);

        resolveCombat();

        GameData gd = harness.getGameData();
        // Player2 takes 2 combat damage: 20 - 2 = 18
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // Player1 gains 2 life from Spirit Link: 20 + 2 = 22
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Life gain equals damage dealt by enchanted creature")
    void lifeGainEqualsDamageDealt() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        // 4/4 creature with Spirit Link attacks unblocked
        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(4);
        bigCreature.setToughness(4);
        Permanent attacker = addReadyCreature(player1, bigCreature);
        attacker.setAttacking(true);
        attachSpiritLink(player1, attacker);

        resolveCombat();

        GameData gd = harness.getGameData();
        // Player2 takes 4 damage: 20 - 4 = 16
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        // Player1 gains 4 life: 10 + 4 = 14
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    // ===== Blocked attacker deals damage to blocker =====

    @Test
    @DisplayName("Controller gains life when enchanted creature deals combat damage to blocker")
    void controllerGainsLifeOnCombatDamageToBlocker() {
        harness.setLife(player1, 20);

        // Grizzly Bears (2/2) with Spirit Link attacks, blocked by 2/2
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attachSpiritLink(player1, attacker);

        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        GameData gd = harness.getGameData();
        // Bears dealt 2 damage to blocker → controller gains 2 life: 20 + 2 = 22
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    // ===== Blocker with Spirit Link =====

    @Test
    @DisplayName("Controller gains life when enchanted blocker deals combat damage")
    void controllerGainsLifeFromEnchantedBlocker() {
        harness.setLife(player2, 20);

        // Player1 attacks with 2/2
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        // Player2 blocks with 2/2 that has Spirit Link
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        attachSpiritLink(player2, blocker);

        resolveCombat();

        GameData gd = harness.getGameData();
        // Blocker dealt 2 damage to attacker → player2 gains 2 life: 20 + 2 = 22
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    // ===== Spirit Link on opponent's creature =====

    @Test
    @DisplayName("Aura controller gains life, not creature controller")
    void auraControllerGainsLifeNotCreatureController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player1 attacks with Grizzly Bears (2/2)
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        // Player2 enchants player1's creature with Spirit Link
        attachSpiritLink(player2, attacker);

        // Player2 blocks with a 2/2 — attacker deals damage to blocker, not to player
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        GameData gd = harness.getGameData();
        // Attacker dealt 2 to blocker → player2 (aura controller) gains 2 life: 20 + 2 = 22
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        // Player1 does NOT gain life (not the aura controller)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== No damage, no life gain =====

    @Test
    @DisplayName("No life gained when enchanted creature does not deal damage")
    void noLifeGainWhenNoDamageDealt() {
        harness.setLife(player1, 20);

        // Creature with Spirit Link does not attack
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        attachSpiritLink(player1, bears);

        // Another creature attacks unblocked
        GrizzlyBears otherBear = new GrizzlyBears();
        Permanent otherAttacker = addReadyCreature(player1, otherBear);
        otherAttacker.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        // Player1 gains no life — enchanted creature didn't deal damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Logs =====

    @Test
    @DisplayName("Spirit Link life gain is logged")
    void spiritLinkLifeGainIsLogged() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        bears.setAttacking(true);
        attachSpiritLink(player1, bears);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("gains") && log.contains("life") && log.contains("Spirit Link"));
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, GrizzlyBears card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void attachSpiritLink(Player controller, Permanent target) {
        SpiritLink card = new SpiritLink();
        Permanent aura = new Permanent(card);
        aura.setAttachedTo(target.getId());
        harness.getGameData().playerBattlefields.get(controller.getId()).add(aura);
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.getGameService().passPriority(harness.getGameData(), player1);
        harness.getGameService().passPriority(harness.getGameData(), player2);
    }
}
