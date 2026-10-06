package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaskiaTheUnyielding.class, GrizzlyBears.class, LightningBolt.class, LoxodonWarhammer.class})
class SaskiaTheUnyieldingTest extends BaseCardTest {

    @Test
    @DisplayName("The chosen player is dealt the combat damage dealt to another player")
    void dealsCombatDamageToChosenPlayer() {
        Permanent saskia = castSaskia();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(saskia.getProtectionFromPlayerIdsPermanently()).containsExactly(player1.getId());
    }

    private Permanent castSaskia() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SaskiaTheUnyielding(), "{B}{R}{G}{W}");
        harness.passBothPriorities();
        return findPermanent(player1, "Saskia the Unyielding");
    }

    @Test
    void ownCombatDamageAlsoDealsDamageToChosenOpponent() {
        Permanent saskia = castSaskia();
        harness.handlePermanentChosen(player1, player2.getId());
        saskia.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 17);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerStillDealsDamageAfterSaskiaLeavesBattlefield() {
        Permanent saskia = castSaskia();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(saskia);
        gd.playerGraveyards.get(player1.getId()).add(saskia.getCard());

        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosingPlayerDoesNotGrantProtectionFromThatPlayer() {
        Permanent saskia = castSaskia();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, saskia.getId());
        harness.passBothPriorities();

        assertThat(saskia.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Saskia the Unyielding");
    }

    @Test
    void opposingCreatureCombatDamageDoesNotTriggerSaskia() {
        castSaskia();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void departedCombatDealerRetainsGrantedLifelinkForTriggeredDamage() {
        castSaskia();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent warhammer = harness.addToBattlefieldAndReturn(player1, new LoxodonWarhammer());
        warhammer.setAttachedTo(bears.getId());
        bears.setAttacking(true);
        harness.setLife(player1, 10);
        harness.setLife(player2, 30);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 25);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }
}