package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinWarrens.class, GoblinHero.class, GrizzlyBears.class})
class GoblinWarrensTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing two Goblins creates three 1/1 red Goblin tokens")
    void createsThreeGoblinTokens() {
        Permanent warrens = harness.addToBattlefieldAndReturn(player1, new GoblinWarrens());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinHero());
        Permanent otherGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinHero());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(warrens), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(goblin.getCard(), otherGoblin.getCard());

        var goblinTokens = findPermanents(player1, "Goblin");
        assertThat(goblinTokens).hasSize(3);
        assertThat(goblinTokens).allSatisfy(p -> {
            assertThat(p.getCard().isToken()).isTrue();
            assertThat(p.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(p.getCard().getPower()).isEqualTo(1);
            assertThat(p.getCard().getToughness()).isEqualTo(1);
            assertThat(p.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(p.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
        });
    }

    @Test
    @DisplayName("Created Goblin tokens can pay for another activation")
    void createdGoblinTokensCanBeSacrificed() {
        Permanent warrens = harness.addToBattlefieldAndReturn(player1, new GoblinWarrens());
        harness.addToBattlefield(player1, new GoblinHero());
        harness.addToBattlefield(player1, new GoblinHero());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(warrens), null, null);
        harness.passBothPriorities();

        var createdTokens = findPermanents(player1, "Goblin");
        assertThat(createdTokens).hasSize(3);
        assertThat(createdTokens).allSatisfy(token -> assertThat(token.getCard().isToken()).isTrue());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(warrens), null, null);
        harness.handlePermanentChosen(player1, createdTokens.get(0).getId());
        harness.handlePermanentChosen(player1, createdTokens.get(1).getId());
        harness.passBothPriorities();

        var remainingTokens = findPermanents(player1, "Goblin");
        assertThat(remainingTokens).hasSize(4);
        assertThat(remainingTokens).allSatisfy(token -> assertThat(token.getCard().isToken()).isTrue());
    }

    @Test
    @DisplayName("Cannot activate without the full mana cost")
    void cannotActivateWithoutFullManaCost() {
        Permanent warrens = harness.addToBattlefieldAndReturn(player1, new GoblinWarrens());
        harness.addToBattlefield(player1, new GoblinHero());
        harness.addToBattlefield(player1, new GoblinHero());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(warrens), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate without two Goblins to sacrifice")
    void cannotActivateWithoutTwoGoblins() {
        Permanent warrens = harness.addToBattlefieldAndReturn(player1, new GoblinWarrens());
        harness.addToBattlefield(player1, new GoblinHero());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(warrens), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("A non-Goblin cannot be chosen when paying the sacrifice cost")
    void cannotChooseNonGoblinAsCost() {
        Permanent warrens = harness.addToBattlefieldAndReturn(player1, new GoblinWarrens());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinHero());
        Permanent otherGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinHero());
        Permanent thirdGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinHero());
        Permanent nonGoblin = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(warrens), null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonGoblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, goblin.getId());
        harness.handlePermanentChosen(player1, otherGoblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(goblin.getCard(), otherGoblin.getCard())
                .doesNotContain(nonGoblin.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(thirdGoblin, nonGoblin);
        assertThat(findPermanents(player1, "Goblin"))
                .hasSize(3)
                .allSatisfy(perm -> assertThat(perm.getCard().isToken()).isTrue());
    }

    @Test
    @DisplayName("Goblins controlled by an opponent cannot pay the sacrifice cost")
    void opponentGoblinsCannotPayCost() {
        Permanent warrens = harness.addToBattlefieldAndReturn(player1, new GoblinWarrens());
        harness.addToBattlefield(player1, new GoblinHero());
        harness.addToBattlefield(player2, new GoblinHero());
        harness.addToBattlefield(player2, new GoblinHero());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(warrens), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }
}
