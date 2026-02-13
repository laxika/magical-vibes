package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StarlightInvokerTest {

    private GameTestHarness harness;
    private Player player1;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Starlight Invoker has correct card properties")
    void hasCorrectProperties() {
        StarlightInvoker card = new StarlightInvoker();

        assertThat(card.getName()).isEqualTo("Starlight Invoker");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(3);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.CLERIC, CardSubtype.MUTANT);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(GainLifeEffect.class);
        GainLifeEffect effect = (GainLifeEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(effect.amount()).isEqualTo(5);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{7}{W}");
    }

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addReadyInvoker(player1);
        harness.addMana(player1, "W", 8);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Starlight Invoker");
    }

    @Test
    @DisplayName("Activating ability does not tap Starlight Invoker")
    void activatingDoesNotTap() {
        Permanent invoker = addReadyInvoker(player1);
        harness.addMana(player1, "W", 8);

        harness.activateAbility(player1, 0, null, null);

        assertThat(invoker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addReadyInvoker(player1);
        harness.addMana(player1, "W", 10);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving ability gains 5 life")
    void resolvingGainsFiveLife() {
        harness.setLife(player1, 20);
        addReadyInvoker(player1);
        harness.addMana(player1, "W", 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Can activate multiple times to gain more life")
    void canActivateMultipleTimes() {
        harness.setLife(player1, 20);
        addReadyInvoker(player1);
        harness.addMana(player1, "W", 16);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(30);
    }

    @Test
    @DisplayName("Resolving ability logs the life gain")
    void resolvingLogsLifeGain() {
        harness.setLife(player1, 20);
        addReadyInvoker(player1);
        harness.addMana(player1, "W", 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("gains") && log.contains("5") && log.contains("life"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyInvoker(player1);
        harness.addMana(player1, "W", 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Helpers =====

    private Permanent addReadyInvoker(Player player) {
        StarlightInvoker card = new StarlightInvoker();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
