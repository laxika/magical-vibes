package com.github.laxika.magicalvibes.cards.f;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.r.RovingKeep;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({ForebodingFruit.class, RovingKeep.class, Island.class})
class ForebodingFruitTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws two cards and loses 2 life without adamant")
    void drawsAndLosesLifeWithoutAdamant() {
        RovingKeep keep = new RovingKeep();
        Island island = new Island();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(keep, island));

        castForebodingFruit(player2.getId(), 1, 2);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keep, island);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Adamant creates a Food token after at least three black mana is spent")
    void adamantCreatesFood() {
        RovingKeep keep = new RovingKeep();
        Island island = new Island();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(keep, island));

        castForebodingFruit(player2.getId(), 3, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keep, island);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new RovingKeep());
        UUID targetId = harness.getPermanentId(player2, "Roving Keep");
        harness.setHand(player1, List.of(new ForebodingFruit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target the caster")
    void canTargetCaster() {
        Island first = new Island();
        Island second = new Island();
        harness.setLibrary(player1, List.of(first, second));

        castForebodingFruit(player1.getId(), 3, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player2, "Food")).isZero();
    }

    @Test
    @DisplayName("Two black mana does not satisfy adamant")
    void twoBlackManaDoesNotCreateFood() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Island(), new Island()));

        castForebodingFruit(player2.getId(), 2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertLife(player2, 18);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Food is sacrificed as a cost and gains its controller three life on resolution")
    void foodCanBeSacrificedImmediately() {
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        castForebodingFruit(player2.getId(), 3, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int foodIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Food"));

        harness.activateAbility(player1, foodIndex, null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A tapped Food cannot pay its tap cost")
    void tappedFoodCannotBeActivated() {
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        castForebodingFruit(player2.getId(), 3, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int foodIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Food"));
        findPermanent(player1, "Food").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, foodIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Food")).isOne();
        harness.assertLife(player1, 20);
    }

    private void castForebodingFruit(UUID targetPlayerId, int blackMana, int colorlessMana) {
        harness.setHand(player1, List.of(new ForebodingFruit()));
        harness.addMana(player1, ManaColor.BLACK, blackMana);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }
}
