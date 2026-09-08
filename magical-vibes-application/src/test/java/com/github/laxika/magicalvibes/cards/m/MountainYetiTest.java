package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MountainYetiTest extends BaseCardTest {

    @Test
    @DisplayName("Mountain Yeti has protection from white")
    void hasProtectionFromWhite() {
        Permanent yeti = harness.addToBattlefieldAndReturn(player1, new MountainYeti());

        assertThat(gqs.hasProtectionFrom(gd, yeti, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, yeti, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Mountain Yeti cannot be blocked when defending player controls a Mountain")
    void cannotBeBlockedWhenDefenderControlsMountain() {
        harness.addToBattlefield(player2, new Mountain());

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent yeti = new Permanent(new MountainYeti());
        yeti.setSummoningSick(false);
        yeti.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(yeti);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int yetiIndex = gd.playerBattlefields.get(player1.getId()).indexOf(yeti);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, yetiIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Mountain Yeti can be blocked when defending player does not control a Mountain")
    void canBeBlockedWhenDefenderDoesNotControlMountain() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent yeti = new Permanent(new MountainYeti());
        yeti.setSummoningSick(false);
        yeti.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(yeti);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

}
