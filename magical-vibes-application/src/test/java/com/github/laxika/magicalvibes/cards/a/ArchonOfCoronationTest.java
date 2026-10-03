package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.ScrapworkRager;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
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

@CardUsed({ArchonOfCoronation.class, Shock.class, GrizzlyBears.class, ScrapworkRager.class, Forest.class, VampireNighthawk.class})
class ArchonOfCoronationTest extends BaseCardTest {

    @Test
    @DisplayName("Its controller becomes the monarch when it enters")
    void becomesMonarchWhenItEnters() {
        harness.enterBattlefieldAndReturn(player1, new ArchonOfCoronation());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Damage does not cause its monarch controller to lose life")
    void damageDoesNotCauseLifeLoss() {
        harness.addToBattlefield(player1, new ArchonOfCoronation());
        gd.monarchPlayerId = player1.getId();
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage causes life loss when its controller is not the monarch")
    void damageCausesLifeLossWhenNotMonarch() {
        harness.addToBattlefield(player1, new ArchonOfCoronation());
        gd.monarchPlayerId = player2.getId();
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Combat damage still changes the monarch")
    void combatDamageStillChangesMonarch() {
        harness.addToBattlefield(player1, new ArchonOfCoronation());
        gd.monarchPlayerId = player1.getId();
        harness.setLife(player1, 20);
        findPermanent(player1, "Archon of Coronation").tap();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Direct life loss still applies")
    void directLifeLossStillApplies() {
        harness.addToBattlefield(player1, new ArchonOfCoronation());
        gd.monarchPlayerId = player1.getId();
        harness.setHand(player1, List.of(new ScrapworkRager()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLibrary(player1, List.of(new Forest()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Lifelink still gains life from damage to the protected monarch")
    void lifelinkStillGainsLife() {
        harness.addToBattlefield(player1, new ArchonOfCoronation());
        gd.monarchPlayerId = player1.getId();
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        Permanent attacker = addCreatureReady(player2, new VampireNighthawk());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("The monarch remains protected until the combat damage trigger resolves")
    void monarchyTransferWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new ArchonOfCoronation());
        gd.monarchPlayerId = player1.getId();
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 20);
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.stack).isNotEmpty();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }
}
