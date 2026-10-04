package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
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

@CardUsed({HoneymoonHearse.class, DawnhartDisciple.class})
class HoneymoonHearseTest extends BaseCardTest {

    @Test
    @DisplayName("Hearse is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent hearse = addHearseReady(player1);

        assertThat(gqs.isCreature(gd, hearse)).isFalse();
    }

    @Test
    @DisplayName("Tapping two untapped creatures animates Hearse as a 5/5")
    void tapTwoCreaturesAnimatesHearse() {
        Permanent hearse = addHearseReady(player1);
        Permanent bearsA = addCreatureReady(player1, new DawnhartDisciple());
        Permanent bearsB = addCreatureReady(player1, new DawnhartDisciple());

        harness.activateAbility(player1, indexOf(player1, hearse), null, null);
        harness.passBothPriorities();

        assertThat(hearse.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, hearse)).isTrue();
        assertThat(gqs.getEffectivePower(gd, hearse)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hearse)).isEqualTo(5);
        assertThat(bearsA.isTapped()).isTrue();
        assertThat(bearsB.isTapped()).isTrue();
        assertThat(hearse.isTapped()).isFalse();
    }

    @Test
    @DisplayName("With more than two creatures, choosing two taps them as cost")
    void choosesTwoOfThreeCreatures() {
        Permanent hearse = addHearseReady(player1);
        addCreatureReady(player1, new DawnhartDisciple());
        addCreatureReady(player1, new DawnhartDisciple());
        Permanent spare = addCreatureReady(player1, new DawnhartDisciple());

        harness.activateAbility(player1, indexOf(player1, hearse), null, null);
        tapCreatures(player1, 2);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hearse)).isTrue();
        assertThat(spare.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate with fewer than two untapped creatures")
    void cannotActivateWithFewerThanTwo() {
        addHearseReady(player1);
        addCreatureReady(player1, new DawnhartDisciple());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent hearse = addHearseReady(player1);
        addCreatureReady(player1, new DawnhartDisciple());
        addCreatureReady(player1, new DawnhartDisciple());

        harness.activateAbility(player1, indexOf(player1, hearse), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hearse)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hearse.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, hearse)).isFalse();
    }

    @Test
    void newlyEnteredCreaturesCanPayAndAnimationWaitsForResolution() {
        Permanent hearse = harness.addToBattlefieldAndReturn(player1, new HoneymoonHearse());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());

        harness.activateAbility(player1, indexOf(player1, hearse), null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, hearse)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hearse)).isTrue();
        assertThat(hearse.isSummoningSick()).isTrue();
    }

    @Test
    void tappedCreaturesCannotPayTheCost() {
        Permanent hearse = addHearseReady(player1);
        addCreatureReady(player1, new DawnhartDisciple());
        Permanent tapped = addCreatureReady(player1, new DawnhartDisciple());
        tapped.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, hearse), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, hearse)).isFalse();
    }

    @Test
    void opponentsCreaturesCannotPayTheCost() {
        Permanent hearse = addHearseReady(player1);
        addCreatureReady(player1, new DawnhartDisciple());
        Permanent opponentCreature = addCreatureReady(player2, new DawnhartDisciple());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, hearse), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    void animatedHearseCanTapItselfForAnotherActivation() {
        Permanent hearse = addHearseReady(player1);
        addCreatureReady(player1, new DawnhartDisciple());
        addCreatureReady(player1, new DawnhartDisciple());
        harness.activateAbility(player1, indexOf(player1, hearse), null, null);
        harness.passBothPriorities();
        Permanent freshCreature = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());

        harness.activateAbility(player1, indexOf(player1, hearse), null, null);
        assertThat(hearse.isTapped()).isTrue();
        assertThat(freshCreature.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hearse)).isTrue();
    }

    private Permanent addHearseReady(Player player) {
        return addCreatureReady(player, new HoneymoonHearse());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void tapCreatures(Player player, int count) {
        List<Permanent> untapped = gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> !p.isTapped())
                .filter(p -> gqs.isCreature(gd, p))
                .limit(count)
                .toList();
        for (Permanent creature : untapped) {
            harness.handlePermanentChosen(player, creature.getId());
        }
    }
}
