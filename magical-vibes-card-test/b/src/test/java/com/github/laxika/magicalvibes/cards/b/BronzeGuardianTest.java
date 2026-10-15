package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.n.NowhereToRun;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PacificationArray;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BronzeGuardian.class, NowhereToRun.class, Ornithopter.class, PacificationArray.class, Shock.class})
class BronzeGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of artifacts you control; toughness stays 5")
    void powerEqualsControlledArtifacts() {
        Permanent guardian = addGuardian(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(5);

        harness.addToBattlefield(player1, new Ornithopter());
        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ward {2} is granted to other artifacts you control")
    void otherControlledArtifactsHaveWard() {
        addGuardian(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castShockAt(player2, artifact, 1);

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Ward {2} can be paid for on another controlled artifact")
    void otherControlledArtifactWardCanBePaid() {
        addGuardian(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castShockAt(player2, artifact, 3);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(artifact.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ward is not granted to an opponent's artifact")
    void opponentArtifactDoesNotHaveWard() {
        addGuardian(player1);
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castShockAt(player1, opponentArtifact, 1);

        harness.assertInGraveyard(player1, "Shock");
        assertThat(opponentArtifact.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addGuardian(Player player) {
        return addCreatureReady(player, new BronzeGuardian());
    }

    @Test
    @DisplayName("Unpaid ward counters Shock and leaves the protected artifact alive")
    void unpaidGrantedWardPreventsDamage() {
        addGuardian(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castShockAt(player2, artifact, 1);

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(artifact.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Bronze Guardian's own ward counters an unpaid opposing spell")
    void ownWardPreventsDamage() {
        Permanent guardian = addGuardian(player1);

        castShockAt(player2, guardian, 1);

        assertThat(guardian.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Bronze Guardian");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("A lone Guardian requires exactly one ward payment")
    void ownWardCanBePaidOnce() {
        Permanent guardian = addGuardian(player1);

        castShockAt(player2, guardian, 3);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(guardian.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Ward does not trigger for spells controlled by the artifact's controller")
    void controllerCanTargetOwnArtifactWithoutWardPayment() {
        Permanent guardian = addGuardian(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castShockAt(player1, artifact, 1);

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Guardians each grant a separate ward payment to another artifact")
    void multipleGrantedWardAbilitiesRequireSeparatePayments() {
        addGuardian(player1);
        addGuardian(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castShockAt(player2, artifact, 3);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(artifact.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("A Guardian also receives ward from another Guardian")
    void guardianReceivesAdditionalWardFromAnotherGuardian() {
        Permanent guardian = addGuardian(player1);
        addGuardian(player1);

        castShockAt(player2, guardian, 3);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(guardian.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Double strike deals the artifact count as damage in both combat damage steps")
    void doubleStrikeDealsDamageTwice() {
        addGuardian(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Noncreature artifacts contribute to power and receive ward against activated abilities")
    void noncreatureArtifactHasWardAgainstActivatedAbility() {
        Permanent guardian = addGuardian(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PacificationArray());
        harness.addToBattlefield(player2, new PacificationArray());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Pacification Array");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying Guardian's ward lets an opposing activated ability resolve")
    void ownWardCanBePaidForActivatedAbility() {
        Permanent guardian = addGuardian(player1);
        harness.addToBattlefield(player2, new PacificationArray());
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, guardian.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(guardian.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Pacification Array");
    }

    @Test
    @DisplayName("Other artifacts lose granted ward after Guardian leaves the battlefield")
    void grantedWardEndsWhenGuardianDies() {
        Permanent guardian = addGuardian(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, guardian.getId());
        harness.castAndResolveInstant(player1, 0, guardian.getId());
        harness.castAndResolveInstant(player1, 0, guardian.getId());
        harness.assertInGraveyard(player1, "Bronze Guardian");

        castShockAt(player2, artifact, 1);

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Nowhere to Run suppresses ward granted to an opposing artifact creature")
    void grantedWardDoesNotTriggerWithNowhereToRun() {
        addGuardian(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addToBattlefield(player2, new NowhereToRun());

        castShockAt(player2, artifact, 1);

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player2, "Shock");
    }

    private void castShockAt(Player caster, Permanent target, int mana) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        if (mana > 1) {
            harness.addMana(caster, ManaColor.COLORLESS, mana - 1);
        }

        harness.castAndResolveInstant(caster, 0, target.getId());
    }
}
