package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DoubleManaPoolEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DoublingCubeTest {

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
    @DisplayName("Doubling Cube has correct card properties")
    void hasCorrectProperties() {
        DoublingCube card = new DoublingCube();

        assertThat(card.getName()).isEqualTo("Doubling Cube");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{2}");
        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{3}");
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(DoubleManaPoolEffect.class);
    }

    // ===== Mana ability resolves immediately (CR 605.1a, CR 605.3a) =====

    @Test
    @DisplayName("Doubling Cube doubles mana pool immediately as a mana ability")
    void doublesManaSingleColor() {
        harness.addToBattlefield(player1, new DoublingCube());
        harness.addMana(player1, ManaColor.GREEN, 7); // 3 to pay cost + 4 to double

        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        // Mana ability — no stack entry
        assertThat(gd.stack).isEmpty();

        // 7 green - 3 for cost = 4 green, doubled = 8 green
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(8);
    }

    @Test
    @DisplayName("Doubling Cube doubles all colors of mana in the pool")
    void doublesMultipleColors() {
        harness.addToBattlefield(player1, new DoublingCube());
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        // 3 generic mana paid from pool (engine picks which colors to spend)
        // After cost, remaining mana is doubled
        // Total before: 5W + 2U + 1R = 8, cost is {3}, so 5 left, doubled = 10
        int totalMana = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(totalMana).isEqualTo(10);
    }

    @Test
    @DisplayName("Doubling Cube with empty pool after paying cost results in no additional mana")
    void doublingEmptyPoolAfterCost() {
        harness.addToBattlefield(player1, new DoublingCube());
        harness.addMana(player1, ManaColor.RED, 3); // exactly enough to pay {3}

        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        // 3 red - 3 for cost = 0, doubled = 0
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Doubling Cube does not affect opponent's mana pool")
    void doesNotAffectOpponent() {
        harness.addToBattlefield(player1, new DoublingCube());
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player2, ManaColor.GREEN, 3);

        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        // Opponent's mana should be untouched
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    // ===== Activation requirements =====

    @Test
    @DisplayName("Doubling Cube cannot be activated without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new DoublingCube());
        harness.addMana(player1, ManaColor.GREEN, 2); // need 3

        assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, null));
    }

    @Test
    @DisplayName("Doubling Cube cannot be activated twice in a turn because it requires tap")
    void cannotActivateTwice() {
        harness.addToBattlefield(player1, new DoublingCube());
        harness.addMana(player1, ManaColor.GREEN, 10);

        harness.activateAbility(player1, 0, null, null);

        // Second activation should fail — already tapped
        assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, null));
    }
}
