package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntanglingTrapTest extends BaseCardTest {

    // ===== Won clash — tap target + it doesn't untap next untap step =====

    @Test
    @DisplayName("Won clash taps target opponent creature and prevents its next untap")
    void wonClashTapsAndLocksUntap() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new EntanglingTrap());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        // Higher mana value on top for player1 (GrizzlyBears MV 2 > Forest MV 0) → player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        harness.getTriggerCollectionService().performClash(gd, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getSkipUntapCount()).isEqualTo(1);
    }

    // ===== Lost clash — tap only, no untap lock =====

    @Test
    @DisplayName("Lost clash taps target but does not prevent untap")
    void lostClashTapsOnly() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new EntanglingTrap());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        // Lower/equal mana value on top for player1 (Forest MV 0 < GrizzlyBears MV 2) → player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        harness.getTriggerCollectionService().performClash(gd, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getSkipUntapCount()).isEqualTo(0);
    }

    // ===== Targeting is restricted to opponent creatures =====

    @Test
    @DisplayName("Clash trigger can only target creatures an opponent controls")
    void targetsOnlyOpponentCreatures() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new EntanglingTrap());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        harness.getTriggerCollectionService().performClash(gd, player1.getId());

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

        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        harness.getTriggerCollectionService().performClash(gd, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }
}
