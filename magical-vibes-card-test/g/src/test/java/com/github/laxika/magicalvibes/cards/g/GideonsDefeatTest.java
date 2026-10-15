package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({GideonsDefeat.class, EliteVanguard.class, GideonOfTheTrials.class, GrizzlyBears.class})
class GideonsDefeatTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a white attacking creature; no life gained when it isn't a Gideon")
    void exilesWhiteAttackerWithoutLifeGain() {
        harness.setLife(player2, 20);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsDefeat()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        // Exiled — gone from the battlefield and not in the graveyard (exile, not destroy).
        harness.assertNotOnBattlefield(player1, "Elite Vanguard");
        harness.assertNotInGraveyard(player1, "Elite Vanguard");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Elite Vanguard"));
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Exiling an attacking Gideon planeswalker gains the caster 5 life")
    void exilingGideonGainsFiveLife() {
        harness.setLife(player2, 20);

        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonOfTheTrials());
        gideon.setCounterCount(CounterType.LOYALTY, 3);
        gideon.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // 0: becomes a 4/4 creature that's still a Gideon planeswalker.
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, gideon)).isTrue();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gideon.setAttacking(true);

        harness.setHand(player2, List.of(new GideonsDefeat()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, gideon.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gideon of the Trials");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gideon of the Trials"));
        harness.assertLife(player2, 25);
    }

    @Test
    @DisplayName("Cannot target a non-white attacking creature")
    void cannotTargetNonWhiteAttacker() {
        // A legal white attacker makes the spell castable; aiming at the green attacker is rejected.
        Permanent legalWhiteAttacker = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        legalWhiteAttacker.setSummoningSick(false);
        legalWhiteAttacker.setAttacking(true);

        Permanent greenAttacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        greenAttacker.setSummoningSick(false);
        greenAttacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsDefeat()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, greenAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");
    }

    @Test
    @DisplayName("Cannot target a white creature that is neither attacking nor blocking")
    void cannotTargetIdleWhiteCreature() {
        // A legal white attacker makes the spell castable; aiming at the idle white creature is rejected.
        Permanent legalWhiteAttacker = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        legalWhiteAttacker.setSummoningSick(false);
        legalWhiteAttacker.setAttacking(true);

        Permanent idleWhite = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        idleWhite.setSummoningSick(false);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsDefeat()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, idleWhite.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");
    }
    @Test
    @DisplayName("Exiles a white blocking creature without gaining life")
    void exilesWhiteBlocker() {
        harness.setLife(player1, 20);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        blocker.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player1, List.of(new GideonsDefeat()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
        harness.assertNotInGraveyard(player2, "Elite Vanguard");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Elite Vanguard"));
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not exile a creature that has left combat before resolution")
    void targetMustStillBeAttackingOrBlockingOnResolution() {
        harness.setLife(player2, 20);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new GideonsDefeat()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

}
