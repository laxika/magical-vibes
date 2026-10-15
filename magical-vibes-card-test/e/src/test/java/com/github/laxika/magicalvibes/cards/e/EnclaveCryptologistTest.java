package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnclaveCryptologist.class, Forest.class})
class EnclaveCryptologistTest extends BaseCardTest {

    @Test
    @DisplayName("At levels one through two Enclave Cryptologist loots when tapped")
    void lootsAtLevelsOneThroughTwo() {
        Permanent cryptologist = addCryptologist();
        levelUp(player1, 1);
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(cryptologist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("At level three Enclave Cryptologist draws a card without discarding")
    void drawsAtLevelThree() {
        Permanent cryptologist = addCryptologist();
        levelUp(player1, 3);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(cryptologist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        Permanent cryptologist = addCryptologist();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(cryptologist.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @Test
    @DisplayName("Without level counters Enclave Cryptologist cannot loot")
    void cannotLootAtLevelZero() {
        Permanent cryptologist = addCryptologist();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");

        assertThat(cryptologist.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("At level two Enclave Cryptologist still draws then discards")
    void lootsAtLevelTwo() {
        Permanent cryptologist = addCryptologist();
        levelUp(player1, 2);
        harness.setHand(player1, List.of(new Forest()));
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1).doesNotContain(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(cryptologist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Levels above three retain the draw ability without discarding")
    void drawsAtLevelFour() {
        Permanent cryptologist = addCryptologist();
        levelUp(player1, 4);
        harness.setHand(player1, List.of());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(cryptologist.isTapped()).isTrue();
        assertThat(cryptologist.getCounterCount(CounterType.LEVEL)).isEqualTo(4);
    }

    @Test
    @DisplayName("Looting with an empty hand discards the newly drawn card")
    void lootsWithEmptyHand() {
        addCryptologist();
        levelUp(player1, 1);
        harness.setHand(player1, List.of());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Level up does not tap and can be used while summoning sick")
    void levelUpWhileSummoningSick() {
        Permanent cryptologist = addCryptologist();
        cryptologist.setSummoningSick(true);

        levelUp(player1, 1);

        assertThat(cryptologist.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(cryptologist.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("The draw ability can be activated on the opponent's turn")
    void drawsOnOpponentsTurn() {
        addCryptologist();
        levelUp(player1, 3);
        harness.setHand(player1, List.of());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Level up costs one generic and one blue mana and adds its counter on resolution")
    void levelUpUsesStackAndPrintedCost() {
        Permanent cryptologist = addCryptologist();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(cryptologist.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(cryptologist.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(cryptologist.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
    }

    private Permanent addCryptologist() {
        return addCreatureReady(player1, new EnclaveCryptologist());
    }

    private void levelUp(Player player, int times) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.BLUE, times * 2);
        for (int i = 0; i < times; i++) {
            harness.activateAbility(player, 0, 0, null, null);
            harness.passBothPriorities();
        }
    }
}
