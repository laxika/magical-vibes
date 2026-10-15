package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BenalishLancer;
import com.github.laxika.magicalvibes.cards.c.CrosissAttendant;
import com.github.laxika.magicalvibes.cards.u.UrborgSkeleton;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HoodedKavu.class, BenalishLancer.class, UrborgSkeleton.class, CrosissAttendant.class})
class HoodedKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Black mana grants Hooded Kavu fear until end of turn")
    void grantsFearUntilEndOfTurn() {
        Permanent kavu = addCreatureReady(player1, new HoodedKavu());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.FEAR)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Fear prevents non-black non-artifact creatures from blocking")
    void fearPreventsNonBlackNonArtifactCreatureFromBlocking() {
        addCreatureReady(player1, new HoodedKavu());
        addCreatureReady(player2, new BenalishLancer());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Fear can be blocked by a black creature")
    void fearCanBeBlockedByBlackCreature() {
        addCreatureReady(player1, new HoodedKavu());
        addCreatureReady(player2, new UrborgSkeleton());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Fear can be blocked by an artifact creature")
    void fearCanBeBlockedByArtifactCreature() {
        addCreatureReady(player1, new HoodedKavu());
        addCreatureReady(player2, new CrosissAttendant());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Fear is gained only when the activated ability resolves and only by its source")
    void fearWaitsForResolutionAndAffectsOnlySource() {
        Permanent kavu = addCreatureReady(player1, new HoodedKavu());
        Permanent otherKavu = addCreatureReady(player1, new HoodedKavu());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.FEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherKavu, Keyword.FEAR)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherKavu, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Hooded Kavu can activate its fear ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new HoodedKavu());
        kavu.setSummoningSick(true);
        kavu.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.FEAR)).isTrue();
        assertThat(kavu.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Without activating fear, a non-black non-artifact creature can block Hooded Kavu")
    void ordinaryCreatureCanBlockWithoutActivation() {
        addCreatureReady(player1, new HoodedKavu());
        addCreatureReady(player2, new BenalishLancer());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
