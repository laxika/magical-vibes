package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteamCatapult.class, AlabornTrooper.class, Swamp.class})
class SteamCatapultTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target tapped creature")
    void resolvingDestroysTargetTappedCreature() {
        setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addTappedTrooper(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alaborn Trooper");
        harness.assertInGraveyard(player2, "Alaborn Trooper");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addUntappedTrooper(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature permanent")
    void cannotTargetTappedNoncreaturePermanent() {
        setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Swamp());
        target.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Does not destroy a target that becomes untapped before resolution")
    void doesNotDestroyTargetThatBecomesUntappedBeforeResolution() {
        setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addTappedTrooper(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Alaborn Trooper");
    }

    @Test
    @DisplayName("Tapping Steam Catapult is part of the activation cost")
    void activationTapsSource() {
        Permanent catapult = setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addTappedTrooper(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(catapult.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate during beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupCatapultOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        Permanent target = addTappedTrooper(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate before attackers are declared in a later combat phase")
    void cannotActivateInLaterCombatPhase() {
        setupCatapultOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        gd.combatPhasesThisTurn = 2;
        Permanent target = addTappedTrooper(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupCatapultOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        Permanent target = addTappedTrooper(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        Permanent target = addTappedTrooper(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private Permanent setupCatapultOnMyTurn(TurnStep step) {
        Permanent catapult = addCreatureReady(player1, new SteamCatapult());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        return catapult;
    }

    private Permanent addTappedTrooper(Player player) {
        Permanent perm = addCreatureReady(player, new AlabornTrooper());
        perm.tap();
        return perm;
    }

    private Permanent addUntappedTrooper(Player player) {
        return addCreatureReady(player, new AlabornTrooper());
    }
}
