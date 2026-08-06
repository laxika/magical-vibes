package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyrexianSplicerTest extends BaseCardTest {

    @Test
    @DisplayName("Moves flying from the first target creature to the second until end of turn")
    void movesFlyingBetweenCreatures() {
        Permanent splicer = addReadySplicer();
        Permanent drake = addCreatureReady(player1, new WindDrake());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyMain(3);

        harness.activateAbilityWithMultiTargets(player1, indexOf(splicer), 0, List.of(drake.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(drake.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(splicer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The keyword swap wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent splicer = addReadySplicer();
        Permanent drake = addCreatureReady(player1, new WindDrake());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyMain(3);

        harness.activateAbilityWithMultiTargets(player1, indexOf(splicer), 0, List.of(drake.getId(), bears.getId()));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(drake.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The first target must be a creature that has the chosen ability")
    void firstTargetMustHaveTheChosenKeyword() {
        Permanent splicer = addReadySplicer();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent drake = addCreatureReady(player1, new WindDrake());
        readyMain(3);

        // Ability 0 is the flying mode; Grizzly Bears has no flying to lose.
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, indexOf(splicer), 0, List.of(bears.getId(), drake.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The trample mode only touches trample, leaving flying alone")
    void trampleModeMovesTrampleOnly() {
        Permanent splicer = addReadySplicer();
        Permanent drake = addCreatureReady(player1, new WindDrake());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        drake.getGrantedKeywords().add(Keyword.TRAMPLE);
        readyMain(3);

        harness.activateAbilityWithMultiTargets(player1, indexOf(splicer), 2, List.of(drake.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(drake.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(drake.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The creature losing the ability still loses it when the gaining target is gone")
    void resolvesPartiallyWhenSecondTargetLeaves() {
        Permanent splicer = addReadySplicer();
        Permanent drake = addCreatureReady(player1, new WindDrake());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyMain(3);

        harness.activateAbilityWithMultiTargets(player1, indexOf(splicer), 0, List.of(drake.getId(), bears.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(drake.hasKeyword(Keyword.FLYING)).isFalse();
    }

    private Permanent addReadySplicer() {
        Permanent splicer = addCreatureReady(player1, new PhyrexianSplicer());
        splicer.setSummoningSick(false);
        return splicer;
    }

    private void readyMain(int mana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, mana);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
