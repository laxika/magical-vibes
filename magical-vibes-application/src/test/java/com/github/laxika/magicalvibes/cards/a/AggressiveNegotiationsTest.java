package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AggressiveNegotiations.class, Forest.class, GrizzlyBears.class})
class AggressiveNegotiationsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a chosen nonland card and puts a counter on a creature you control")
    void exilesNonlandAndAddsCounter() {
        Card forest = new Forest();
        Card bearsInHand = new GrizzlyBears();
        harness.setHand(player2, List.of(forest, bearsInHand));
        harness.setHand(player1, List.of(new AggressiveNegotiations()));
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), targetCreature.getId()));

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bearsInHand);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest);
        assertThat(targetCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The creature target is optional")
    void creatureTargetIsOptional() {
        Card bearsInHand = new GrizzlyBears();
        harness.setHand(player2, List.of(bearsInHand));
        harness.setHand(player1, List.of(new AggressiveNegotiations()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId()));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bearsInHand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only an opponent can be targeted")
    void onlyOpponentCanBeTargeted() {
        harness.setHand(player1, List.of(new AggressiveNegotiations()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void emptyHandDoesNotPreventCounterPlacement() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new AggressiveNegotiations()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void landOnlyHandDoesNotPreventCounterPlacement() {
        Card forest = new Forest();
        harness.setHand(player2, List.of(forest));
        harness.setHand(player1, List.of(new AggressiveNegotiations()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotTargetOpponentsCreatureForCounter() {
        harness.setHand(player1, List.of(new AggressiveNegotiations()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player2.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreatureForCounter() {
        harness.setHand(player1, List.of(new AggressiveNegotiations()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player2.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillExilesWhenCreatureTargetLeavesBeforeResolution() {
        Card bearsInHand = new GrizzlyBears();
        harness.setHand(player2, List.of(bearsInHand));
        harness.setHand(player1, List.of(new AggressiveNegotiations()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, List.of(player2.getId(), creature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bearsInHand);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
