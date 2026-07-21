package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToHandReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheScorpionGodTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever a creature with a -1/-1 counter dies, draw a card")
    void drawsWhenCreatureWithMinusOneCounterDies() {
        harness.addToBattlefield(player1, new TheScorpionGod());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        setDeck(player1, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID dyingId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, dyingId);
        harness.passBothPriorities(); // Shock resolves → creature dies → draw trigger
        harness.passBothPriorities(); // resolve draw

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw when the dying creature had no -1/-1 counter")
    void doesNotDrawWithoutMinusOneCounter() {
        harness.addToBattlefield(player1, new TheScorpionGod());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setDeck(player1, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        int handBeforeCast = gd.playerHands.get(player1.getId()).size();

        UUID dyingId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, dyingId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeCast - 1);
    }

    @Test
    @DisplayName("{1}{B}{R}: Put a -1/-1 counter on another target creature")
    void activatedAbilityPutsMinusOneCounter() {
        Permanent scorpion = harness.addToBattlefieldAndReturn(player1, new TheScorpionGod());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(scorpion.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Activated ability cannot target The Scorpion God itself")
    void cannotTargetSelf() {
        Permanent scorpion = harness.addToBattlefieldAndReturn(player1, new TheScorpionGod());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, scorpion.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }

    @Test
    @DisplayName("When The Scorpion God dies, it returns to hand at the beginning of the next end step")
    void diesReturnsToHandAtNextEndStep() {
        Permanent scorpion = harness.addToBattlefieldAndReturn(player1, new TheScorpionGod());
        Card scorpionCard = scorpion.getCard();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves — dies, death trigger on stack
        harness.passBothPriorities(); // resolve death trigger — register delayed return

        assertThat(gd.getDelayedActions(DelayedGraveyardToHandReturn.class)).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(scorpionCard.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gs.advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(scorpionCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(scorpionCard.getId()));
        assertThat(gd.getDelayedActions(DelayedGraveyardToHandReturn.class)).isEmpty();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
