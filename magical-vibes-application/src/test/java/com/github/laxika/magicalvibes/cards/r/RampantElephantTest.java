package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArdentSoldier;
import com.github.laxika.magicalvibes.cards.c.CoastalTower;
import com.github.laxika.magicalvibes.cards.s.ShivanZombie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RampantElephant.class, ArdentSoldier.class, CoastalTower.class, ShivanZombie.class, Repulse.class})
class RampantElephantTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability spends one green mana without tapping Rampant Elephant")
    void activatingAbilityPaysGreenManaWithoutTappingSource() {
        Permanent elephant = addReadyElephant(player1);
        Permanent target = addCreatureReady(player2, new ArdentSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(elephant.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Resolving the ability makes the target creature need to block Rampant Elephant")
    void resolvingAbilityAddsMustBlockRestriction() {
        Permanent elephant = addReadyElephant(player1);
        Permanent target = addCreatureReady(player2, new ArdentSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMustBlockIds()).contains(elephant.getId());
    }

    @Test
    @DisplayName("The target creature must block Rampant Elephant when it attacks")
    void targetedCreatureMustBlockElephant() {
        Permanent elephant = addReadyElephant(player1);
        Permanent blocker = addCreatureReady(player2, new ArdentSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        elephant.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("The target creature may remain unblocked when Rampant Elephant is not attacking")
    void noRequirementIfElephantIsNotAttacking() {
        addReadyElephant(player1);
        Permanent otherCreature = addCreatureReady(player1, new ArdentSoldier());
        Permanent target = addCreatureReady(player2, new ArdentSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        otherCreature.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyElephant(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CoastalTower());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("The must-block restriction resets at end of turn")
    void restrictionResetsAtEndOfTurn() {
        Permanent elephant = addReadyElephant(player1);
        Permanent target = addCreatureReady(player2, new ArdentSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMustBlockIds()).contains(elephant.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getMustBlockIds()).doesNotContain(elephant.getId());
    }

    @Test
    @DisplayName("A tapped target is not required to block")
    void tappedTargetDoesNotHaveToBlock() {
        Permanent elephant = addReadyElephant(player1);
        Permanent target = addCreatureReady(player2, new ArdentSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        target.tap();
        elephant.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Elephant can activate its ability")
    void tappedSummoningSickSourceCanActivate() {
        Permanent elephant = harness.addToBattlefieldAndReturn(player1, new RampantElephant());
        elephant.setSummoningSick(true);
        elephant.tap();
        Permanent target = addCreatureReady(player2, new ArdentSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMustBlockIds()).contains(elephant.getId());
        assertThat(elephant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability can target a creature its controller controls")
    void canTargetOwnCreature() {
        Permanent elephant = addReadyElephant(player1);
        Permanent target = addCreatureReady(player1, new ArdentSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMustBlockIds()).contains(elephant.getId());
    }

    @Test
    @DisplayName("Protection from white prevents targeting even though activation costs green mana")
    void cannotTargetCreatureWithProtectionFromWhite() {
        addReadyElephant(player1);
        Permanent target = addCreatureReady(player2, new ShivanZombie());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature required to block two Elephants may choose either one")
    void competingRequirementsAllowBlockingSecondElephant() {
        Permanent first = addReadyElephant(player1);
        Permanent second = addReadyElephant(player1);
        Permanent blocker = addCreatureReady(player2, new ArdentSoldier());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, blocker.getId());
        harness.passBothPriorities();

        first.setAttacking(true);
        second.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The ability does not affect a target that leaves before resolution")
    void targetLeavesBeforeResolution() {
        addReadyElephant(player1);
        Permanent target = addCreatureReady(player2, new ArdentSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Repulse()));
        harness.setLibrary(player2, List.of(new ArdentSoldier()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ardent Soldier");
        harness.assertInHand(player2, "Ardent Soldier");
        assertThat(target.getMustBlockIds()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the source does not force the target to block another attacker")
    void sourceLeavesBeforeResolution() {
        Permanent elephant = addReadyElephant(player1);
        Permanent otherAttacker = addCreatureReady(player1, new ArdentSoldier());
        Permanent target = addCreatureReady(player2, new ArdentSoldier());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Repulse()));
        harness.setLibrary(player2, List.of(new ArdentSoldier()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, elephant.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rampant Elephant");
        harness.assertInHand(player1, "Rampant Elephant");
        otherAttacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    private Permanent addReadyElephant(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new RampantElephant());
    }
}
