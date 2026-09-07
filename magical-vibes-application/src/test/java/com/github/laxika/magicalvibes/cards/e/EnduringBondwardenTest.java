package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnduringBondwarden.class, GrizzlyBears.class, Assassinate.class})
class EnduringBondwardenTest extends BaseCardTest {

    @Test
    @DisplayName("Backup grants another creature a death trigger that moves all its counters to your creature")
    void backupGrantsCounterTransferToAnotherCreature() {
        Permanent backedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolveBackup(backedCreature);
        backedCreature.tap();

        destroyTappedCreature(backedCreature);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Backup targeting the source does not grant a duplicate death trigger")
    void backupTargetingSourceDoesNotDuplicateDeathTrigger() {
        Permanent bondwarden = castAndResolveBackup(null);
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bondwarden.tap();

        destroyTappedCreature(bondwarden);

        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Backup's granted death trigger expires at end of turn")
    void grantedDeathTriggerExpiresAtEndOfTurn() {
        Permanent backedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAndResolveBackup(backedCreature);
        backedCreature.tap();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        destroyTappedCreature(backedCreature);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castAndResolveBackup(Permanent target) {
        harness.setHand(player1, List.of(new EnduringBondwarden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent bondwarden = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof EnduringBondwarden)
                .findFirst()
                .orElseThrow();
        harness.handlePermanentChosen(player1, target == null ? bondwarden.getId() : target.getId());
        harness.passBothPriorities();
        return bondwarden;
    }

    private void destroyTappedCreature(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gs.playCard(gd, player1, 0, 0, creature.getId(), null);
        harness.passBothPriorities();
    }
}
