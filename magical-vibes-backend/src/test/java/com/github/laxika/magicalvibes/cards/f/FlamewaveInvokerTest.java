package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlamewaveInvokerTest {

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
    @DisplayName("Flamewave Invoker has correct card properties")
    void hasCorrectProperties() {
        FlamewaveInvoker card = new FlamewaveInvoker();

        assertThat(card.getName()).isEqualTo("Flamewave Invoker");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.GOBLIN, CardSubtype.MUTANT);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{7}{R}");
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(DealDamageToTargetPlayerEffect.class);
        DealDamageToTargetPlayerEffect effect = (DealDamageToTargetPlayerEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(effect.damage()).isEqualTo(5);
    }

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Flamewave Invoker");
    }

    @Test
    @DisplayName("Activating ability does not tap Flamewave Invoker")
    void activatingDoesNotTap() {
        Permanent invoker = addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(invoker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 10);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving ability deals 5 damage to target player")
    void resolvingDealsFiveDamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Can target self with the ability")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Can activate multiple times to deal more damage")
    void canActivateMultipleTimes() {
        harness.setLife(player2, 20);
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 16);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Resolving ability logs the damage")
    void resolvingLogsDamage() {
        harness.setLife(player2, 20);
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("takes") && log.contains("5") && log.contains("damage"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target a creature with the ability")
    void cannotTargetCreature() {
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);
        harness.addToBattlefield(player2, new GrizzlyBears());
        GameData gd = harness.getGameData();
        Permanent bear = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    // ===== Helpers =====

    private Permanent addReadyInvoker(Player player) {
        FlamewaveInvoker card = new FlamewaveInvoker();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

