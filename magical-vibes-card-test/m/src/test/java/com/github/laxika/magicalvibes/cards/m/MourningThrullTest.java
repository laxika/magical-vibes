package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Delirium;
import com.github.laxika.magicalvibes.cards.s.Stratozeppelid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MourningThrull.class, Stratozeppelid.class, Delirium.class})
class MourningThrullTest extends BaseCardTest {

    private void addAttacker(MourningThrull card) {
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setAttacking(true);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Combat damage to a player gains that much life")
    void combatDamageToPlayerGainsLife() {
        MourningThrull thrull = new MourningThrull();
        thrull.setPower(3);
        addAttacker(thrull);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Gains life from damage dealt to a blocker even when it dies in combat")
    void gainsLifeFromCreatureDamageWhenItDies() {
        addAttacker(new MourningThrull());
        harness.setLife(player1, 20);

        Permanent blocker = addCreatureReady(player2, new Stratozeppelid());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        harness.assertInGraveyard(player1, "Mourning Thrull");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not gain life when it deals no combat damage")
    void noLifeWhenBlockedByLargerAndDealsNoDamage() {
        MourningThrull thrull = new MourningThrull();
        thrull.setPower(0);
        addAttacker(thrull);
        harness.setLife(player1, 20);

        Permanent blocker = addCreatureReady(player2, new Stratozeppelid());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Noncombat damage also gains that much life")
    void noncombatDamageAlsoGainsLife() {
        Permanent thrull = addCreatureReady(player2, new MourningThrull());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Delirium()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, thrull.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Life gain waits for the damage trigger to resolve")
    void lifeGainUsesDamageAmountCapturedWhenTriggered() {
        MourningThrull card = new MourningThrull();
        card.setPower(3);
        addAttacker(card);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(gd.stack).hasSize(1);

        card.setPower(5);
        resolveAllTriggers();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("A blocking Thrull gains life for its controller even when it dies")
    void blockingThrullGainsLifeForDefendingController() {
        Permanent attacker = addCreatureReady(player1, new Stratozeppelid());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MourningThrull());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Mourning Thrull");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
    }
}
