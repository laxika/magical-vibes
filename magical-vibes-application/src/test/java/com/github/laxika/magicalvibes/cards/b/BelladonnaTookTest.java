package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrenkosCommand;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelladonnaTook.class, GrizzlyBears.class, KrenkosCommand.class})
class BelladonnaTookTest extends BaseCardTest {

    @Test
    @DisplayName("Token-entry resolutions gain life, draw, then put counters on creatures")
    void tokenEntryResolutionsProgressThroughAllThreeEffects() {
        harness.addToBattlefield(player1, new BelladonnaTook());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new KrenkosCommand(), new KrenkosCommand()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Belladonna Took")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Goblin")).allSatisfy(goblin ->
                assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    @DisplayName("Each token in a batch triggers separately and effects depend on resolution order")
    void batchTokensResolveIndividually() {
        harness.addToBattlefield(player1, new BelladonnaTook());
        harness.setLibrary(player1, List.of(new BelladonnaTook()));
        harness.setHand(player1, List.of(new KrenkosCommand()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Belladonna Took")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent tokens only trigger their controller's Belladonna")
    void opponentTokensDoNotTriggerOrAdvanceOwnAbility() {
        harness.addToBattlefield(player1, new BelladonnaTook());
        harness.addToBattlefield(player2, new BelladonnaTook());
        harness.setLibrary(player1, List.of(new BelladonnaTook()));
        harness.setLibrary(player2, List.of(new BelladonnaTook()));
        harness.setHand(player2, List.of(new KrenkosCommand()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player2, 0, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new KrenkosCommand(), new KrenkosCommand()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();
        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        assertThat(findPermanent(player1, "Belladonna Took")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player2, "Belladonna Took")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player2, "Goblin")).hasSize(2).allSatisfy(goblin ->
                assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @DisplayName("Resolution count resets on a new turn")
    void resolutionCountResetsEachTurn() {
        harness.addToBattlefield(player1, new BelladonnaTook());
        harness.setLibrary(player1, List.of(new BelladonnaTook(), new BelladonnaTook(),
                new BelladonnaTook(), new BelladonnaTook()));
        harness.setLibrary(player2, List.of(new BelladonnaTook(), new BelladonnaTook()));
        harness.setHand(player1, List.of(new KrenkosCommand()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();
        harness.assertLife(player1, 21);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new KrenkosCommand()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Belladonna Took")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Goblin")).hasSize(4).allSatisfy(goblin ->
                assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }
}
