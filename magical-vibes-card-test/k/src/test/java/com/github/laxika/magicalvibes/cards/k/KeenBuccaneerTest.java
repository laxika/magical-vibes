package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BounceOff;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@DisplayName("Keen Buccaneer")
@CardUsed({KeenBuccaneer.class, Forest.class, BounceOff.class})
class KeenBuccaneerTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust draws, discards, and puts a +1/+1 counter on it")
    void exhaustAbility() {
        Permanent buccaneer = addBuccaneer();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(buccaneer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each exhaust ability can be activated only once")
    void cannotExhaustTwice() {
        addBuccaneer();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        addExhaustMana();
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Exhaust can be activated while tapped and summoning sick, and can discard the drawn card")
    void exhaustWhileTappedAndSummoningSick() {
        Permanent buccaneer = harness.addToBattlefieldAndReturn(player1, new KeenBuccaneer());
        buccaneer.setSummoningSick(true);
        buccaneer.tap();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(buccaneer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnCard);
        assertThat(buccaneer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(buccaneer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exhaust is spent as soon as activated, before resolution")
    void cannotExhaustAgainWhileOnStack() {
        addBuccaneer();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        addExhaustMana();
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
    }

    @Test
    @DisplayName("Exhaust still draws and discards if its source leaves the battlefield")
    void exhaustResolvesAfterSourceBounced() {
        Permanent buccaneer = addBuccaneer();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player2, List.of(new BounceOff()));
        addExhaustMana();
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castInstant(player2, 0, buccaneer.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard, buccaneer.getCard());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(buccaneer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addBuccaneer() {
        Permanent buccaneer = harness.addToBattlefieldAndReturn(player1, new KeenBuccaneer());
        buccaneer.setSummoningSick(false);
        return buccaneer;
    }

    private void addExhaustMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
