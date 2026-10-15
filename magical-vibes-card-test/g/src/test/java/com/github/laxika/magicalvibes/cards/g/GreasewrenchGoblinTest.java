package com.github.laxika.magicalvibes.cards.g;

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

@DisplayName("Greasewrench Goblin")
@CardUsed({GreasewrenchGoblin.class, Forest.class})
class GreasewrenchGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust discards up to two, draws that many, and adds a counter")
    void exhaustAbility() {
        Permanent goblin = addReadyGoblin();
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The exhaust ability can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        addReadyGoblin();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Choosing zero discards still adds a counter without drawing")
    void zeroDiscardsStillAddsCounter() {
        Permanent goblin = addReadyGoblin();
        Forest kept = new Forest();
        Forest libraryCard = new Forest();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(libraryCard));
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discarding one card draws exactly one before adding the counter")
    void discardsAndDrawsOne() {
        Permanent goblin = addReadyGoblin();
        Forest discarded = new Forest();
        Forest kept = new Forest();
        Forest drawn = new Forest();
        Forest remaining = new Forest();
        harness.setHand(player1, List.of(discarded, kept));
        harness.setLibrary(player1, List.of(drawn, remaining));
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn, remaining);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exhaust works with an empty hand and while summoning sick")
    void emptyHandAndSummoningSicknessDoNotPreventExhaust() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GreasewrenchGoblin());
        goblin.setSummoningSick(true);
        harness.setHand(player1, List.of());
        Forest libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addReadyGoblin() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GreasewrenchGoblin());
        goblin.setSummoningSick(false);
        return goblin;
    }

    private void addExhaustMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
