package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.c.CentaurHealer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GruesomeFate;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SerraAscendant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferisProtection.class, GrizzlyBears.class, GruesomeFate.class, Shock.class,
        CentaurHealer.class, ActOfTreason.class, SerraAscendant.class, Plains.class})
class TeferisProtectionTest extends BaseCardTest {

    @Test
    @DisplayName("Phases out your permanents, locks your life total, grants protection, and exiles itself")
    void resolvesAllEffects() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 10);
        castProtection();
        assertThat(gqs.canPlayerLifeChange(gd, player1.getId())).isFalse();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentBears);
        assertThat(gd.playersWithLifeTotalCantChangeUntilNextTurn).contains(player1.getId());
        assertThat(gd.playersWithProtectionFromEverythingUntilNextTurn).contains(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Teferi's Protection"));

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from everything");
    }

    @Test
    @DisplayName("Life gain is prevented until your next turn")
    void lifeGainIsPreventedUntilNextTurn() {
        harness.setLife(player1, 10);
        castProtection();
        assertThat(gqs.canPlayerLifeChange(gd, player1.getId())).isFalse();

        harness.castFromHand(player1, new CentaurHealer(), "{1}{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Protection and life lock end at your next turn")
    void effectsEndAtNextTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castProtection();
        assertThat(gqs.canPlayerLifeChange(gd, player1.getId())).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playersWithLifeTotalCantChangeUntilNextTurn).contains(player1.getId());
        assertThat(gd.playersWithProtectionFromEverythingUntilNextTurn).contains(player1.getId());

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gd.playersWithLifeTotalCantChangeUntilNextTurn).doesNotContain(player1.getId());
        assertThat(gd.playersWithProtectionFromEverythingUntilNextTurn).doesNotContain(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gqs.canPlayerLifeChange(gd, player1.getId())).isTrue();
    }

    private void castProtection() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new TeferisProtection(), "{2}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Nontargeted life loss cannot change the protected player's life total")
    void nontargetedLifeLossIsPrevented() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 10);
        castProtection();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GruesomeFate(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player2, "Gruesome Fate");
    }

    @Test
    @DisplayName("Prevented combat damage does not grant the attacking creature's controller lifelink life")
    void combatDamageIsPreventedRatherThanOnlyLockingLife() {
        addCreatureReady(player2, new SerraAscendant());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        castProtection();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A borrowed creature phases in under its prior controller after temporary control expires")
    void temporaryControlExpiresWhileCreatureIsPhasedOut() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, bears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);

        castProtection();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(bears);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("A spell already targeting a phased-out creature does not resolve")
    void pendingSpellLosesItsCreatureTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, bears.getId());
        harness.castFromHand(player1, new TeferisProtection(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(bears);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }
}
