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

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
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

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
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
