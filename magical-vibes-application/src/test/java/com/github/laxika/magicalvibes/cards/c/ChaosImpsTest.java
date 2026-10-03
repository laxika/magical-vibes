package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChaosImps.class, CatacombSlug.class, CorpsejackMenace.class})
class ChaosImpsTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting unleash puts a +1/+1 counter on it as it enters")
    void unleashedEntersWithCounter() {
        castImps(true);

        Permanent imps = findPermanent(player1, "Chaos Imps");
        assertThat(imps.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, imps)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, imps)).isEqualTo(6);
    }

    @Test
    @DisplayName("Declining unleash leaves it without a counter and without trample")
    void decliningLeavesNoCounter() {
        castImps(false);

        Permanent imps = findPermanent(player1, "Chaos Imps");
        assertThat(imps.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, imps, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("It gains trample only while it has a +1/+1 counter")
    void trampleTracksCounter() {
        Permanent imps = addCreatureReady(player1, new ChaosImps());
        assertThat(gqs.hasKeyword(gd, imps, Keyword.TRAMPLE)).isFalse();

        imps.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, imps, Keyword.TRAMPLE)).isTrue();

        imps.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, imps, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An unleashed Chaos Imps can't block")
    void unleashedCantBlock() {
        Permanent imps = addCreatureReady(player1, new ChaosImps());
        imps.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new CatacombSlug());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without a +1/+1 counter it blocks normally")
    void blocksWithoutCounter() {
        addCreatureReady(player1, new ChaosImps());
        addCreatureReady(player2, new CatacombSlug());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(findPermanent(player1, "Chaos Imps").isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Corpsejack Menace doubles the unleash counter as Chaos Imps enters")
    void unleashCounterIsDoubled() {
        harness.addToBattlefield(player1, new CorpsejackMenace());

        castImps(true);

        Permanent imps = findPermanent(player1, "Chaos Imps");
        assertThat(imps.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, imps, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Removing the last +1/+1 counter restores the ability to block")
    void canBlockAfterLastCounterIsRemoved() {
        castImps(true);
        Permanent imps = findPermanent(player1, "Chaos Imps");
        imps.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        addCreatureReady(player2, new CatacombSlug());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(imps.isBlocking()).isTrue();
        assertThat(gqs.hasKeyword(gd, imps, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A counter of another type neither grants trample nor prevents blocking")
    void otherCounterTypeDoesNotEnableUnleashRestrictions() {
        Permanent imps = addCreatureReady(player1, new ChaosImps());
        imps.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        addCreatureReady(player2, new CatacombSlug());

        assertThat(gqs.hasKeyword(gd, imps, Keyword.TRAMPLE)).isFalse();
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(imps.isBlocking()).isTrue();
    }

    private void castImps(boolean unleash) {
        harness.setHand(player1, List.of(new ChaosImps()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, unleash);
    }
}
