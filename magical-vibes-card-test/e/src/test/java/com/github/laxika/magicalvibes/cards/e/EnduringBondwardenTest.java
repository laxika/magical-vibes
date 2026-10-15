package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.IchorDrinker;
import com.github.laxika.magicalvibes.cards.v.VanquishTheWeak;
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

@CardUsed({EnduringBondwarden.class, IchorDrinker.class, VanquishTheWeak.class})
class EnduringBondwardenTest extends BaseCardTest {

    @Test
    @DisplayName("Backup grants another creature a death trigger that moves all its counters to your creature")
    void backupGrantsCounterTransferToAnotherCreature() {
        Permanent backedCreature = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new IchorDrinker());
        castAndResolveBackup(backedCreature);

        destroyCreature(backedCreature);

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
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());

        destroyCreature(bondwarden);

        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Backup's granted death trigger expires at end of turn")
    void grantedDeathTriggerExpiresAtEndOfTurn() {
        Permanent backedCreature = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        castAndResolveBackup(backedCreature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        destroyCreature(backedCreature);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The native death ability triggers even without counters")
    void nativeDeathAbilityTriggersWithoutCounters() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        Permanent bondwarden = castAndResolveBackup(recipient);

        destroyCreature(bondwarden);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The granted death ability still triggers after the backup counter is removed")
    void grantedDeathAbilityTriggersWithoutCounters() {
        Permanent backedCreature = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        castAndResolveBackup(backedCreature);
        backedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        destroyCreature(backedCreature);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Backup can grant the death ability to an opponent's creature, controlled by that opponent")
    void opponentControlsGrantedDeathAbility() {
        Permanent backedCreature = harness.addToBattlefieldAndReturn(player2, new IchorDrinker());
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new IchorDrinker());
        Permanent ourCreature = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        castAndResolveBackup(backedCreature);

        destroyCreature(backedCreature);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, ourCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player2, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ourCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The death ability puts every kind of counter onto its target using the death snapshot")
    void deathAbilityTransfersAllCounterKinds() {
        Permanent bondwarden = castAndResolveBackup(null);
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        bondwarden.setCounterCount(CounterType.CHARGE, 3);
        bondwarden.setCounterCount(CounterType.STUN, 2);

        destroyCreature(bondwarden);
        harness.handlePermanentChosen(player1, recipient.getId());
        bondwarden.setCounterCount(CounterType.CHARGE, 0);
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(recipient.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(recipient.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two backups grant independent death abilities that each put all counters on a target")
    void multipleBackupsGrantIndependentDeathAbilities() {
        Permanent backedCreature = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        castAndResolveBackup(backedCreature);
        castAndResolveBackup(backedCreature);

        destroyCreature(backedCreature);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Negative counters are included and can kill the recipient")
    void negativeCountersCanKillRecipient() {
        Permanent bondwarden = harness.addToBattlefieldAndReturn(player1, new EnduringBondwarden());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        bondwarden.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bondwarden, recipient);
        harness.assertInGraveyard(player1, "Enduring Bondwarden");
        harness.assertInGraveyard(player1, "Ichor Drinker");
    }

    private Permanent castAndResolveBackup(Permanent target) {
        harness.castFromHand(player1, new EnduringBondwarden(), "{W}");
        harness.passBothPriorities();
        Permanent bondwarden = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof EnduringBondwarden)
                .findFirst()
                .orElseThrow();
        harness.handlePermanentChosen(player1, target == null ? bondwarden.getId() : target.getId());
        harness.passBothPriorities();
        return bondwarden;
    }

    private void destroyCreature(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new VanquishTheWeak()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
    }
}
