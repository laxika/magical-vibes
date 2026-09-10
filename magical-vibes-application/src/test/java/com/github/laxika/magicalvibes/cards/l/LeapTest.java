package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.HornOfGreed;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SkyshroudArcher;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Leap.class, SpinedWurm.class, HornOfGreed.class, SkyshroudArcher.class, Shock.class})
class LeapTest extends BaseCardTest {

    @Test
    @DisplayName("Grants flying to the target creature and draws a card")
    void grantsFlyingAndDraws() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());
        harness.setHand(player1, List.of(new Leap()));
        harness.setLibrary(player1, List.of(new SpinedWurm()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, wurm.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wurm, Keyword.FLYING)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new SpinedWurm());
        harness.setHand(player1, List.of(new Leap()));
        harness.setLibrary(player1, List.of(new SpinedWurm()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, wurm.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wurm, Keyword.FLYING)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());
        harness.setHand(player1, List.of(new Leap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, wurm.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wurm, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not draw when the target is removed before resolution")
    void doesNotDrawWhenTargetIsRemovedBeforeResolution() {
        Permanent archer = harness.addToBattlefieldAndReturn(player1, new SkyshroudArcher());
        harness.setHand(player1, List.of(new Leap()));
        harness.setLibrary(player1, List.of(new SpinedWurm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, archer.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, archer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(archer.getId()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent horn = harness.addToBattlefieldAndReturn(player1, new HornOfGreed());
        harness.addToBattlefield(player1, new SpinedWurm());
        harness.setHand(player1, List.of(new Leap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, horn.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
