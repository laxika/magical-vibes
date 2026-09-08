package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KingsAssassin.class, GrizzlyBears.class, Forest.class})
class KingsAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target tapped creature")
    void resolvingDestroysTargetTappedCreature() {
        setupAssassinOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addTappedBears(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        setupAssassinOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addUntappedBears(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature permanent")
    void cannotTargetTappedNoncreaturePermanent() {
        setupAssassinOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addCreatureReady(player2, new Forest());
        target.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Can target a tapped creature it controls")
    void canTargetTappedCreatureItControls() {
        setupAssassinOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addTappedBears(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy a target that becomes untapped before resolution")
    void targetMustRemainTappedAtResolution() {
        setupAssassinOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addTappedBears(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can activate during beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupAssassinOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        Permanent target = addTappedBears(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupAssassinOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        Permanent target = addTappedBears(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        setupAssassinOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        Permanent target = addTappedBears(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private void setupAssassinOnMyTurn(TurnStep step) {
        addCreatureReady(player1, new KingsAssassin());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }

    private Permanent addTappedBears(Player player) {
        Permanent perm = addCreatureReady(player, new GrizzlyBears());
        perm.tap();
        return perm;
    }

    private Permanent addUntappedBears(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
