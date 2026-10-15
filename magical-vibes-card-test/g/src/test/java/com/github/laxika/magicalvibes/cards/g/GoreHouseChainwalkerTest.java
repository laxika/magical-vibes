package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DeadbridgeGoliath;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoreHouseChainwalker.class, DeadbridgeGoliath.class, GarruksPackleader.class})
class GoreHouseChainwalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting unleash puts a +1/+1 counter on it as it enters")
    void unleashedEntersWithCounter() {
        castChainwalker(true);

        Permanent chainwalker = findPermanent(player1, "Gore-House Chainwalker");
        assertThat(chainwalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, chainwalker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chainwalker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining unleash leaves it without a counter")
    void decliningLeavesNoCounter() {
        castChainwalker(false);

        Permanent chainwalker = findPermanent(player1, "Gore-House Chainwalker");
        assertThat(chainwalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An unleashed Gore-House Chainwalker can't block")
    void unleashedCantBlock() {
        Permanent chainwalker = addCreatureReady(player1, new GoreHouseChainwalker());
        chainwalker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new DeadbridgeGoliath());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without a +1/+1 counter it blocks normally")
    void blocksWithoutCounter() {
        addCreatureReady(player1, new GoreHouseChainwalker());
        addCreatureReady(player2, new DeadbridgeGoliath());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(findPermanent(player1, "Gore-House Chainwalker").isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction is block-only — an unleashed Gore-House Chainwalker can still attack")
    void unleashedCanStillAttack() {
        harness.setLife(player2, 20);
        Permanent chainwalker = addCreatureReady(player1, new GoreHouseChainwalker());
        chainwalker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Removing the unleash counter restores the ability to block")
    void blocksAfterUnleashCounterIsRemoved() {
        castChainwalker(true);
        Permanent chainwalker = findPermanent(player1, "Gore-House Chainwalker");
        chainwalker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        addCreatureReady(player2, new DeadbridgeGoliath());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(chainwalker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An unleashed Chainwalker enters with power three for entry triggers")
    void unleashCounterCountsForEntryTriggers() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        harness.setLibrary(player1, List.of(new GoreHouseChainwalker()));

        castChainwalker(true);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining unleash does not trigger a power-three entry ability")
    void decliningUnleashDoesNotTriggerPackleader() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        harness.setLibrary(player1, List.of(new GoreHouseChainwalker()));

        castChainwalker(false);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castChainwalker(boolean unleash) {
        harness.castFromHand(player1, new GoreHouseChainwalker(), "{1}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, unleash);
    }
}
