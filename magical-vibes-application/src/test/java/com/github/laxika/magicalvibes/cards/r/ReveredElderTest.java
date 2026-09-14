package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.k.KrisMage;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.COLORLESS;
import static com.github.laxika.magicalvibes.model.ManaColor.RED;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReveredElder.class, FreshVolunteers.class, KrisMage.class})
class ReveredElderTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability adds a prevention shield to Revered Elder")
    void activationAddsPreventionShield() {
        Permanent elder = addReadyElder();
        harness.addMana(player1, COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(elder.getDamagePreventionShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The next 1 damage to Revered Elder is prevented")
    void preventsNextDamage() {
        Permanent elder = addReadyElder();
        Permanent attacker = addCreatureReady(player2, new FreshVolunteers());
        harness.addMana(player1, COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(elder.getMarkedDamage()).isEqualTo(1);
        assertThat(elder.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("The prevention shield also prevents noncombat damage")
    void preventsNoncombatDamage() {
        Permanent elder = addReadyElder();
        addCreatureReady(player2, new KrisMage());
        harness.setHand(player2, List.of(new FreshVolunteers()));
        harness.addMana(player1, COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player2, RED, 1);
        harness.activateAbility(player2, 0, 0, null, elder.getId());
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(elder.getMarkedDamage()).isZero();
        assertThat(elder.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("The prevention shield expires at end of turn")
    void shieldExpiresAtEndOfTurn() {
        Permanent elder = addReadyElder();
        harness.addMana(player1, COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elder.getDamagePreventionShield()).isZero();
    }

    private Permanent addReadyElder() {
        return addCreatureReady(player1, new ReveredElder());
    }
}
