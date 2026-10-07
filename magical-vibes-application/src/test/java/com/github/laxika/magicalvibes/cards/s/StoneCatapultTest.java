package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlertShuInfantry;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WeiInfantry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoneCatapult.class, AlertShuInfantry.class, WeiInfantry.class, Forest.class})
class StoneCatapultTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target tapped nonblack creature")
    void resolvingDestroysTappedNonblackCreature() {
        Permanent catapult = setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addTappedCreature(player2, new AlertShuInfantry());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(catapult.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alert Shu Infantry");
        harness.assertInGraveyard(player2, "Alert Shu Infantry");
    }

    @Test
    @DisplayName("Cannot target a tapped black creature")
    void cannotTargetBlackCreature() {
        setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addTappedCreature(player2, new WeiInfantry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target an untapped nonblack creature")
    void cannotTargetUntappedCreature() {
        setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addCreatureReady(player2, new AlertShuInfantry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupCatapultOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        Permanent target = addTappedCreature(player2, new AlertShuInfantry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        Permanent target = addTappedCreature(player2, new AlertShuInfantry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Can target a tapped nonblack creature its controller controls")
    void canTargetOwnTappedNonblackCreature() {
        setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addTappedCreature(player1, new AlertShuInfantry());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Target must still be tapped when the ability resolves")
    void targetMustStillBeTappedOnResolution() {
        setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addTappedCreature(player2, new AlertShuInfantry());

        harness.activateAbility(player1, 0, null, target.getId());
        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Alert Shu Infantry");
        harness.assertNotInGraveyard(player2, "Alert Shu Infantry");
    }

    @Test
    @DisplayName("Cannot activate while Stone Catapult is tapped")
    void cannotActivateWhileTapped() {
        Permanent catapult = setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        catapult.tap();
        Permanent target = addTappedCreature(player2, new AlertShuInfantry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate during the beginning of combat before attackers are declared")
    void canActivateAtBeginningOfCombat() {
        setupCatapultOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        Permanent target = addTappedCreature(player2, new AlertShuInfantry());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alert Shu Infantry");
        harness.assertInGraveyard(player2, "Alert Shu Infantry");
    }

    @Test
    @DisplayName("Cannot activate the tap ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent catapult = setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        catapult.setSummoningSick(true);
        Permanent target = addTappedCreature(player2, new AlertShuInfantry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(catapult.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability resolves after Stone Catapult leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent catapult = setupCatapultOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addTappedCreature(player2, new AlertShuInfantry());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(catapult);
        gd.playerGraveyards.get(player1.getId()).add(catapult.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alert Shu Infantry");
        harness.assertInGraveyard(player2, "Alert Shu Infantry");
    }

    private Permanent setupCatapultOnMyTurn(TurnStep step) {
        Permanent catapult = addCreatureReady(player1, new StoneCatapult());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        return catapult;
    }

    private Permanent addTappedCreature(Player player, Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.tap();
        return permanent;
    }
}
