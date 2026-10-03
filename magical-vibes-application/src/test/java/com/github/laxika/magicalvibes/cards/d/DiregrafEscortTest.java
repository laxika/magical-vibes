package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.n.NettleSwine;
import com.github.laxika.magicalvibes.cards.p.PeelFromReality;
import com.github.laxika.magicalvibes.cards.u.UndeadExecutioner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiregrafEscort.class, NettleSwine.class, UndeadExecutioner.class, PeelFromReality.class})
class DiregrafEscortTest extends BaseCardTest {

    private Permanent castAndPairWithSwine() {
        Permanent swine = harness.addToBattlefieldAndReturn(player1, new NettleSwine());
        harness.castFromHand(player1, new DiregrafEscort(), "{G}");
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, swine.getId());
        return swine;
    }

    private Permanent findEscort() {
        return findPermanent(player1, "Diregraf Escort");
    }

    @Test
    @DisplayName("While paired, both creatures have protection from Zombies")
    void pairedBothHaveProtectionFromZombies() {
        Permanent swine = castAndPairWithSwine();
        Permanent escort = findEscort();
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new UndeadExecutioner());

        assertThat(escort.getPairedWithId()).isEqualTo(swine.getId());
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, escort, zombie)).isTrue();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, swine, zombie)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Diregraf Escort has no protection from Zombies")
    void unpairedHasNoProtection() {
        harness.addToBattlefield(player1, new DiregrafEscort());
        Permanent escort = findEscort();
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new UndeadExecutioner());

        assertThat(escort.getPairedWithId()).isNull();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, escort, zombie)).isFalse();
    }

    @Test
    @DisplayName("Protection does not extend to non-Zombie sources")
    void noProtectionFromNonZombies() {
        Permanent swine = castAndPairWithSwine();
        Permanent escort = findEscort();
        Permanent vanilla = harness.addToBattlefieldAndReturn(player2, new NettleSwine());

        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, escort, vanilla)).isFalse();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, swine, vanilla)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both creatures unprotected")
    void decliningLeavesUnprotected() {
        Permanent swine = harness.addToBattlefieldAndReturn(player1, new NettleSwine());
        harness.castFromHand(player1, new DiregrafEscort(), "{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent escort = findEscort();
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new UndeadExecutioner());

        assertThat(escort.getPairedWithId()).isNull();
        assertThat(swine.getPairedWithId()).isNull();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, escort, zombie)).isFalse();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, swine, zombie)).isFalse();
    }

    @Test
    @DisplayName("Soulbond does not trigger when Escort enters without another creature")
    void enteringAloneDoesNotTriggerSoulbond() {
        harness.castFromHand(player1, new DiregrafEscort(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findEscort().getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("Escort can pair when another creature enters")
    void pairsWithEnteringCreature() {
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new DiregrafEscort());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new UndeadExecutioner());
        harness.castFromHand(player1, new NettleSwine(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent swine = findPermanent(player1, "Nettle Swine");
        assertThat(escort.getPairedWithId()).isEqualTo(swine.getId());
        assertThat(swine.getPairedWithId()).isEqualTo(escort.getId());
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, escort, zombie)).isTrue();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, swine, zombie)).isTrue();
    }

    @Test
    @DisplayName("Declining the other creature's entry leaves Escort unpaired")
    void declinesPairingWithEnteringCreature() {
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new DiregrafEscort());
        harness.castFromHand(player1, new NettleSwine(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(escort.getPairedWithId()).isNull();
        assertThat(findPermanent(player1, "Nettle Swine").getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("A partner leaving ends pairing and protection")
    void returningPartnerToHandEndsProtection() {
        Permanent swine = castAndPairWithSwine();
        Permanent escort = findEscort();
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new UndeadExecutioner());
        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, List.of(swine.getId(), zombie.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nettle Swine");
        assertThat(escort.getPairedWithId()).isNull();
        Permanent otherZombie = harness.addToBattlefieldAndReturn(player2, new UndeadExecutioner());
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, escort, otherZombie)).isFalse();
    }

    @Test
    @DisplayName("A dead Zombie's triggered ability cannot target either protected creature")
    void zombieDeathTriggerCannotTargetPair() {
        Permanent swine = castAndPairWithSwine();
        Permanent escort = findEscort();
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new NettleSwine());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new UndeadExecutioner());
        zombie.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextDeathTriggerTarget(gd));

        harness.assertInGraveyard(player2, "Undead Executioner");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(legalTarget.getId())
                .doesNotContain(escort.getId(), swine.getId());
    }

    @Test
    @DisplayName("Protection prevents combat damage from a Zombie blocked by Escort")
    void preventsZombieCombatDamage() {
        castAndPairWithSwine();
        Permanent escort = findEscort();
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new UndeadExecutioner());
        zombie.setSummoningSick(false);
        zombie.setAttacking(true);
        escort.setBlocking(true);
        escort.addBlockingTarget(0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Diregraf Escort");
        assertThat(escort.getMarkedDamage()).isZero();
        assertThat(zombie.getMarkedDamage()).isEqualTo(1);
    }
}
