package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MidnightGuardTest extends BaseCardTest {

    

    @Test
    @DisplayName("Untaps when controller's other creature enters")
    void untapsWhenControllerCreatureEnters() {
        Permanent guard = addCreatureReady(player1, new MidnightGuard());
        guard.tap();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Midnight Guard");

        harness.passBothPriorities(); // resolve Midnight Guard trigger

        assertThat(guard.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps when opponent's creature enters")
    void untapsWhenOpponentCreatureEnters() {
        Permanent guard = addCreatureReady(player1, new MidnightGuard());
        guard.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Midnight Guard trigger

        assertThat(guard.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when Midnight Guard itself enters")
    void doesNotTriggerForSelfEntering() {
        harness.setHand(player1, List.of(new MidnightGuard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when a noncreature permanent enters")
    void doesNotTriggerForNonCreaturePermanent() {
        Permanent guard = addCreatureReady(player1, new MidnightGuard());
        guard.tap();

        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);

        harness.passBothPriorities(); // resolve artifact spell

        assertThat(gd.stack).isEmpty();
        assertThat(guard.isTapped()).isTrue();
    }
}
