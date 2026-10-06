package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShipwreckSinger.class, GrizzlyBears.class, Forest.class})
class ShipwreckSingerTest extends BaseCardTest {

    @Test
    @DisplayName("First ability forces an opponent's creature to attack without tapping Shipwreck Singer")
    void forcesOpponentsCreatureToAttack() {
        Permanent singer = addCreatureReady(player1, new ShipwreckSinger());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(singer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("First ability cannot target your own creature or a noncreature permanent")
    void firstAbilityRestrictsTargets() {
        addCreatureReady(player1, new ShipwreckSinger());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Second ability gives -1/-1 to all attacking creatures only")
    void weakensAttackingCreatures() {
        addCreatureReady(player1, new ShipwreckSinger());
        Permanent ownAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentAttacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player2, new GrizzlyBears());
        ownAttacker.setAttacking(true);
        opponentAttacker.setAttacking(true);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ownAttacker.getEffectivePower()).isEqualTo(1);
        assertThat(ownAttacker.getEffectiveToughness()).isEqualTo(1);
        assertThat(opponentAttacker.getEffectivePower()).isEqualTo(1);
        assertThat(opponentAttacker.getEffectiveToughness()).isEqualTo(1);
        assertThat(nonattacker.getEffectivePower()).isEqualTo(2);
        assertThat(nonattacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The first ability works while Shipwreck Singer is tapped and summoning sick")
    void firstAbilityDoesNotRequireTapOrHaste() {
        Permanent singer = harness.addToBattlefieldAndReturn(player1, new ShipwreckSinger());
        singer.setSummoningSick(true);
        singer.setTapped(true);
        Permanent target = addCreatureReady(player2, new ShipwreckSinger());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(singer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An able creature cannot be omitted after the first ability resolves")
    void forcedCreatureMustBeDeclaredAsAttacker() {
        addCreatureReady(player1, new ShipwreckSinger());
        Permanent target = addCreatureReady(player2, new ShipwreckSinger());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThat(target.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A tapped creature is not required to attack by the first ability")
    void forcedTappedCreatureMayStayOutOfCombat() {
        addCreatureReady(player1, new ShipwreckSinger());
        Permanent target = addCreatureReady(player2, new ShipwreckSinger());
        target.setTapped(true);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of());

        assertThat(target.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("The second ability cannot be activated while tapped or summoning sick")
    void secondAbilityRequiresUntappedReadySinger() {
        Permanent singer = harness.addToBattlefieldAndReturn(player1, new ShipwreckSinger());
        singer.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        singer.setSummoningSick(false);
        singer.setTapped(true);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }

    @Test
    @DisplayName("The second ability selects attackers on resolution and lasts after combat")
    void penaltySnapshotsAttackersAndExpiresAtCleanup() {
        Permanent singer = addCreatureReady(player1, new ShipwreckSinger());
        Permanent attacker = addCreatureReady(player2, new ShipwreckSinger());
        Permanent laterAttacker = addCreatureReady(player2, new ShipwreckSinger());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(singer.isTapped()).isTrue();
        attacker.setAttacking(true);
        harness.passBothPriorities();
        attacker.setAttacking(false);
        laterAttacker.setAttacking(true);

        assertThat(attacker.getEffectivePower()).isZero();
        assertThat(attacker.getEffectiveToughness()).isEqualTo(1);
        assertThat(laterAttacker.getEffectivePower()).isEqualTo(1);
        assertThat(laterAttacker.getEffectiveToughness()).isEqualTo(2);

        laterAttacker.setAttacking(false);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(attacker.getEffectivePower()).isEqualTo(1);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack requirement expires during cleanup")
    void attackRequirementDoesNotCarryIntoNextTurn() {
        addCreatureReady(player1, new ShipwreckSinger());
        Permanent target = addCreatureReady(player2, new ShipwreckSinger());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isMustAttackThisTurn()).isFalse();
        declareAttackers(player2, List.of());
        assertThat(target.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Multiple penalties stack and destroy an attacker with zero toughness")
    void repeatedPenaltiesKillAttacker() {
        addCreatureReady(player1, new ShipwreckSinger());
        addCreatureReady(player1, new ShipwreckSinger());
        Permanent attacker = addCreatureReady(player2, new ShipwreckSinger());
        attacker.setAttacking(true);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(attacker.getEffectiveToughness()).isEqualTo(1);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Shipwreck Singer");
        harness.assertInGraveyard(player2, "Shipwreck Singer");
    }
}
