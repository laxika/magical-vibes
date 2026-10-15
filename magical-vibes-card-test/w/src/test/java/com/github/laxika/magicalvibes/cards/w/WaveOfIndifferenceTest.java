package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DaruLancer;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({WaveOfIndifference.class, DaruLancer.class, Mountain.class})
class WaveOfIndifferenceTest extends BaseCardTest {

    @Test
    @DisplayName("X target creatures can't block this turn")
    void targetCreaturesCannotBlockThisTurn() {
        addCreatureReady(player1, new DaruLancer());
        Permanent firstTarget = addCreatureReady(player2, new DaruLancer());
        Permanent secondTarget = addCreatureReady(player2, new DaruLancer());
        Permanent untargeted = addCreatureReady(player2, new DaruLancer());

        castWave(2, List.of(firstTarget.getId(), secondTarget.getId()));

        assertThat(firstTarget.isCantBlockThisTurn()).isTrue();
        assertThat(secondTarget.isCantBlockThisTurn()).isTrue();
        assertThat(untargeted.isCantBlockThisTurn()).isFalse();

        declareAttackersAndPrepareBlockers(List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X must equal the number of creature targets")
    void requiresExactlyXTargets() {
        Permanent target = addCreatureReady(player2, new DaruLancer());
        harness.setHand(player1, List.of(new WaveOfIndifference()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new WaveOfIndifference()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 resolves without targets")
    void xZeroResolvesWithoutTargets() {
        Permanent creature = addCreatureReady(player2, new DaruLancer());

        castWave(0, List.of());

        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void canTargetCreatureYouControl() {
        Permanent target = addCreatureReady(player1, new DaruLancer());

        castWave(1, List.of(target.getId()));

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction wears off at the end of the turn")
    void restrictionWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new DaruLancer());

        castWave(1, List.of(target.getId()));
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The same creature cannot be chosen twice")
    void cannotRepeatCreatureTarget() {
        Permanent target = addCreatureReady(player2, new DaruLancer());
        harness.setHand(player1, List.of(new WaveOfIndifference()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2,
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A remaining legal target is affected when another target leaves")
    void resolvesForRemainingLegalTarget() {
        Permanent departing = addCreatureReady(player2, new DaruLancer());
        Permanent remaining = addCreatureReady(player2, new DaruLancer());
        harness.setHand(player1, List.of(new WaveOfIndifference()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 2, List.of(departing.getId(), remaining.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(departing);
        gd.playerHands.get(player2.getId()).add(departing.getCard());
        harness.passBothPriorities();

        assertThat(remaining.isCantBlockThisTurn()).isTrue();
        assertThat(departing.isCantBlockThisTurn()).isFalse();
        harness.assertInGraveyard(player1, "Wave of Indifference");
    }

    @Test
    @DisplayName("X above 100 still requires exactly X creature targets")
    void largeXCannotUseFewerTargets() {
        List<java.util.UUID> targetIds = java.util.stream.IntStream.range(0, 100)
                .mapToObj(i -> addCreatureReady(player2, new DaruLancer()).getId())
                .toList();
        harness.setHand(player1, List.of(new WaveOfIndifference()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 101);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 101, targetIds))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWave(int xValue, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new WaveOfIndifference()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorcery(player1, 0, xValue, targetIds);
        harness.passBothPriorities();
    }
}
