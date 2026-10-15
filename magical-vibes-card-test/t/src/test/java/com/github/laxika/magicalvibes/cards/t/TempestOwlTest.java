package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TempestOwl.class, AngelicChorus.class, FountainOfYouth.class, GrizzlyBears.class})
class TempestOwlTest extends BaseCardTest {

    @Test
    void kickedEtbTapsUpToThreeTargetPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new AngelicChorus());

        castKicked(List.of(creature.getId(), artifact.getId(), enchantment.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
        assertThat(enchantment.isTapped()).isTrue();
    }

    @Test
    void nonKickedEtbDoesNotTapTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TempestOwl()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void kickedEtbCanChooseZeroTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castKicked(List.of());

        assertThat(target.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Tempest Owl");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void kickedEtbCanChooseOnlyOneTarget() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castKicked(List.of(chosen.getId()));

        assertThat(chosen.isTapped()).isTrue();
        assertThat(unchosen.isTapped()).isFalse();
    }

    @Test
    void kickedEtbCanChooseTwoTargetsIncludingAnAlreadyTappedPermanent() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        second.tap();
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castKicked(List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(unchosen.isTapped()).isFalse();
    }

    @Test
    void kickedEtbCanTargetTheOwlItselfAfterItEnters() {
        prepareKickedCast();
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent owl = findPermanent(player1, "Tempest Owl");

        harness.handlePermanentChosen(player1, owl.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(owl.isTapped()).isTrue();
    }

    private void castKicked(List<java.util.UUID> targetIds) {
        prepareKickedCast();
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        for (java.util.UUID targetId : targetIds) {
            harness.handlePermanentChosen(player1, targetId);
        }
        if (targetIds.size() < 3) {
            harness.handlePermanentChosen(player1, player1.getId());
        }
        resolveAllTriggers();
    }

    private void prepareKickedCast() {
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TempestOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
