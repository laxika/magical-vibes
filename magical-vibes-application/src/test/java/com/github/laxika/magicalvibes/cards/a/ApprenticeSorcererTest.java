package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MuckRats;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ApprenticeSorcerer.class, MuckRats.class})
class ApprenticeSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player and taps")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        setupSorcererOnMyTurn(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(findPermanent(player1, "Apprentice Sorcerer").isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, destroying a 1/1")
    void deals1DamageDestroying1Toughness() {
        setupSorcererOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new MuckRats());

        UUID targetId = harness.getPermanentId(player2, "Muck Rats");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Muck Rats");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new ApprenticeSorcerer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenAlreadyTapped() {
        Permanent sorcerer = addCreatureReady(player1, new ApprenticeSorcerer());
        sorcerer.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Fizzles if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        setupSorcererOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new MuckRats());

        UUID targetId = harness.getPermanentId(player2, "Muck Rats");
        harness.activateAbility(player1, 0, null, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Can activate at the beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupSorcererOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupSorcererOnMyTurn(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new ApprenticeSorcerer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Can activate during upkeep")
    void canActivateDuringUpkeep() {
        harness.setLife(player2, 20);
        setupSorcererOnMyTurn(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cannot activate during the postcombat main phase or end step")
    void cannotActivateLaterInTurn() {
        setupSorcererOnMyTurn(TurnStep.POSTCOMBAT_MAIN);

        for (TurnStep step : new TurnStep[]{TurnStep.POSTCOMBAT_MAIN, TurnStep.END_STEP}) {
            harness.forceStep(step);
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("before attackers are declared");
            assertThat(findPermanent(player1, "Apprentice Sorcerer").isTapped()).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    @DisplayName("Can target itself and dies from its own damage")
    void canTargetItself() {
        setupSorcererOnMyTurn(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player1, "Apprentice Sorcerer"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Apprentice Sorcerer");
        harness.assertInGraveyard(player1, "Apprentice Sorcerer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability still deals damage after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.setLife(player2, 20);
        setupSorcererOnMyTurn(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    private void setupSorcererOnMyTurn(TurnStep step) {
        addCreatureReady(player1, new ApprenticeSorcerer());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
