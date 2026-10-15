package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
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

@CardUsed({SwoopingLookout.class, CrawlingChorus.class})
class SwoopingLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new SwoopingLookout());
        lookout.setSummoningSick(false);

        harness.addToBattlefield(player2, new CrawlingChorus());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Vigilance prevents Swooping Lookout from tapping when attacking")
    void vigilancePreventsTapWhenAttacking() {
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new SwoopingLookout());
        lookout.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(lookout.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Another flying creature can block Swooping Lookout even while summoning sick")
    void flyingCreatureCanBlock() {
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new SwoopingLookout());
        lookout.setSummoningSick(false);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SwoopingLookout());
        blocker.setSummoningSick(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow a summoning-sick creature to attack")
    void summoningSicknessStillPreventsAttacking() {
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new SwoopingLookout());
        lookout.setSummoningSick(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Vigilance does not allow a tapped creature to attack")
    void tappedCreatureCannotAttack() {
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new SwoopingLookout());
        lookout.setSummoningSick(false);
        lookout.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
