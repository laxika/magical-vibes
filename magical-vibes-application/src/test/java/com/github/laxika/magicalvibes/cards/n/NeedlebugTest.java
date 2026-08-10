package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NeedlebugTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast during combat thanks to flash")
    void canCastDuringCombat() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Needlebug()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Protection from artifacts prevents blocking by an artifact creature")
    void protectionPreventsBlockingByArtifactCreature() {
        Permanent needlebug = new Permanent(new Needlebug());
        needlebug.setSummoningSick(false);
        needlebug.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(needlebug);

        Permanent ironMyr = new Permanent(new IronMyr());
        ironMyr.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ironMyr);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts allows blocking by a non-artifact creature")
    void protectionAllowsBlockingByNonArtifactCreature() {
        Permanent needlebug = new Permanent(new Needlebug());
        needlebug.setSummoningSick(false);
        needlebug.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(needlebug);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bears.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Static protection from artifacts persists after resetModifiers")
    void staticProtectionPersistsAfterReset() {
        harness.addToBattlefield(player1, new Needlebug());

        Permanent needlebug = findPermanent(player1, "Needlebug");
        needlebug.resetModifiers();

        Permanent artifactSource = new Permanent(new IronMyr());
        Permanent nonArtifactSource = new Permanent(new GrizzlyBears());

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, needlebug, artifactSource)).isTrue();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, needlebug, nonArtifactSource)).isFalse();
    }
}
