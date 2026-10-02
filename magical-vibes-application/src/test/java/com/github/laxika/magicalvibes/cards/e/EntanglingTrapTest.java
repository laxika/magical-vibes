package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CloudcrownOak;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EntanglingTrap.class, CloudcrownOak.class, Forest.class})
class EntanglingTrapTest extends BaseCardTest {

    // ===== Won clash — tap target + it doesn't untap next untap step =====

    @Test
    @DisplayName("Won clash taps target opponent creature and prevents its next untap")
    void wonClashTapsAndLocksUntap() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new EntanglingTrap());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak());
        Permanent otherOpponentCreature = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak());

        // Higher mana value on top for player1 (Cloudcrown Oak MV 4 > Forest MV 0) → player1 wins.
        harness.setLibrary(player1, List.of(new CloudcrownOak()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getSkipUntapCount()).isEqualTo(1);
        assertThat(otherOpponentCreature.isTapped()).isFalse();
        assertThat(otherOpponentCreature.getSkipUntapCount()).isZero();

        harness.performUntapStep(player2);
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getSkipUntapCount()).isZero();

        harness.performUntapStep(player2);
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    // ===== Lost clash — tap only, no untap lock =====

    @Test
    @DisplayName("Lost clash taps target but does not prevent untap")
    void lostClashTapsOnly() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new EntanglingTrap());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak());

        // Lower mana value on top for player1 (Forest MV 0 < Cloudcrown Oak MV 4) → player1 loses.
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new CloudcrownOak()));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getSkipUntapCount()).isEqualTo(0);

        harness.performUntapStep(player2);
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tied clash taps target but does not prevent untap")
    void tiedClashTapsOnly() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new EntanglingTrap());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak());

        harness.setLibrary(player1, List.of(new CloudcrownOak()));
        harness.setLibrary(player2, List.of(new CloudcrownOak()));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getSkipUntapCount()).isZero();

        harness.performUntapStep(player2);
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Clash trigger fizzles if its target is no longer controlled by an opponent")
    void targetMustStillBeControlledByOpponentOnResolution() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new EntanglingTrap());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak());

        harness.setLibrary(player1, List.of(new CloudcrownOak()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));

        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Simulate a control-changing effect resolving above the pending clash trigger.
        gd.playerBattlefields.get(player2.getId()).remove(opponentCreature);
        gd.playerBattlefields.get(player1.getId()).add(opponentCreature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));

        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(opponentCreature.getSkipUntapCount()).isZero();
    }

    // ===== Targeting is restricted to opponent creatures =====

    @Test
    @DisplayName("Clash trigger can only target creatures an opponent controls")
    void targetsOnlyOpponentCreatures() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new EntanglingTrap());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CloudcrownOak());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak());

        harness.setLibrary(player1, List.of(new CloudcrownOak()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());
    }

    // ===== No opponent creature — trigger is skipped =====

    @Test
    @DisplayName("Clash trigger is skipped when the opponent controls no creatures")
    void triggerSkippedWhenNoTargets() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new EntanglingTrap());

        harness.setLibrary(player1, List.of(new CloudcrownOak()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().performClash(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }
}
