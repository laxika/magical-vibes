package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HellkiteCharger.class, KrakenHatchling.class, IntoTheRoil.class})
class HellkiteChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying the attack trigger untaps attackers and grants an additional combat phase")
    void payingUntapsAttackersAndGrantsExtraCombat() {
        Permanent charger = addCreatureReady(player1, new HellkiteCharger());
        Permanent attacker = addCreatureReady(player1, new KrakenHatchling());
        Permanent nonAttacker = addCreatureReady(player1, new KrakenHatchling());
        nonAttacker.tap();
        harness.addMana(player1, ManaColor.RED, 7);

        gd.combatPhasesThisTurn = 1;
        declareAttackers(List.of(0, 1));
        assertThat(charger.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(charger.isTapped()).isFalse();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(nonAttacker.isTapped()).isTrue();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the attack trigger leaves creatures tapped and grants no additional combat phase")
    void decliningLeavesAttackersTapped() {
        Permanent charger = addCreatureReady(player1, new HellkiteCharger());
        Permanent attacker = addCreatureReady(player1, new KrakenHatchling());

        gd.combatPhasesThisTurn = 1;
        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(charger.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    @DisplayName("Seven mana with only one red cannot pay for the attack trigger")
    void paymentRequiresTwoRedMana() {
        Permanent charger = addCreatureReady(player1, new HellkiteCharger());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        gd.combatPhasesThisTurn = 1;
        declareAttackers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
        });

        assertThat(charger.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack trigger still untaps other attackers after the Charger leaves")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent charger = addCreatureReady(player1, new HellkiteCharger());
        Permanent attacker = addCreatureReady(player1, new KrakenHatchling());
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 7);
        gd.combatPhasesThisTurn = 1;
        declareAttackers(List.of(0, 1));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.castInstant(player2, 0, charger.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
        });

        harness.assertNotOnBattlefield(player1, "Hellkite Charger");
        harness.assertInHand(player1, "Hellkite Charger");
        assertThat(attacker.isTapped()).isFalse();
        assertThat(attacker.isAttacking()).isTrue();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying in an additional combat grants another combat and untaps immediately")
    void canPayAgainInAdditionalCombat() {
        Permanent charger = addCreatureReady(player1, new HellkiteCharger());
        harness.addMana(player1, ManaColor.RED, 7);
        gd.combatPhasesThisTurn = 2;
        declareAttackers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
        });

        assertThat(charger.isTapped()).isFalse();
        assertThat(charger.isAttacking()).isTrue();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }
}
