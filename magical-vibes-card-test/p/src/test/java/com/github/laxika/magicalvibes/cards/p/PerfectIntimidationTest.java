package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.u.UnwelcomeSprite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PerfectIntimidation.class, UnwelcomeSprite.class})
class PerfectIntimidationTest extends BaseCardTest {

    @Test
    @DisplayName("Exile mode makes the target opponent exile two cards of their choice")
    void exileModeExilesTwoCardsFromTargetOpponentsHand() {
        harness.setHand(player2, List.of(new UnwelcomeSprite(), new UnwelcomeSprite()));
        cast(new int[]{0}, List.of(player2.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Counter mode removes every counter from the target creature")
    void counterModeRemovesAllCountersFromTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UnwelcomeSprite());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setCounterCount(CounterType.CHARGE, 1);
        cast(new int[]{1}, List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Both modes resolve with their independent targets")
    void bothModesResolve() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UnwelcomeSprite());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new UnwelcomeSprite(), new UnwelcomeSprite()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setHand(player1, List.of(new PerfectIntimidation()));
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), creature.getId()), null);
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The exile mode cannot target its controller")
    void exileModeRequiresOpponent() {
        harness.addToBattlefield(player2, new UnwelcomeSprite());
        harness.setHand(player1, List.of(new PerfectIntimidation()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(player1.getId()), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    void exileModeExilesTheOnlyCardWhenOpponentHasOneCard() {
        UnwelcomeSprite card = new UnwelcomeSprite();
        harness.setHand(player2, List.of(card));
        cast(new int[]{0}, List.of(player2.getId()));

        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentChoosesWhichTwoCardsToExileFromLargerHand() {
        UnwelcomeSprite retained = new UnwelcomeSprite();
        UnwelcomeSprite firstExiled = new UnwelcomeSprite();
        UnwelcomeSprite secondExiled = new UnwelcomeSprite();
        harness.setHand(player2, List.of(retained, firstExiled, secondExiled));
        cast(new int[]{0}, List.of(player2.getId()));

        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(firstExiled, secondExiled);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void bothModesStillRemoveCountersWhenOpponentsHandIsEmpty() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new UnwelcomeSprite());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setCounterCount(CounterType.FLYING, 1);
        creature.setCounterCount(CounterType.STUN, 3);
        harness.setHand(player2, List.of());

        cast(new int[]{0, 1}, List.of(player2.getId(), creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bothModesStillExileCardsWhenCreatureTargetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UnwelcomeSprite());
        harness.setHand(player2, List.of(new UnwelcomeSprite(), new UnwelcomeSprite()));
        harness.setHand(player1, List.of(new PerfectIntimidation()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), creature.getId()), null);
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new PerfectIntimidation()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targetIds, null);
        harness.passBothPriorities();
    }
}
