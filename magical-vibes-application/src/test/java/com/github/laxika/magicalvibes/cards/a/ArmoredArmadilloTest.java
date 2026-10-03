package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SterlingKeykeeper;
import com.github.laxika.magicalvibes.cards.t.TakeUpTheShield;
import com.github.laxika.magicalvibes.cards.t.ThunderSalvo;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmoredArmadillo.class, SterlingKeykeeper.class, TakeUpTheShield.class, ThunderSalvo.class})
class ArmoredArmadilloTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability adds the Armadillo's toughness to its power")
    void activatedAbilityBoostsByToughness() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, armadillo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, armadillo)).isEqualTo(4);
    }

    @Test
    @DisplayName("The activated ability boost wears off at end of turn")
    void activatedAbilityBoostExpiresAtEndOfTurn() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, armadillo)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, armadillo)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when its controller does not pay")
    void wardCountersUnpaidSpell() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ThunderSalvo()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, armadillo.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Thunder Salvo");
    }

    @Test
    @DisplayName("Ward lets an opponent's spell resolve when its controller pays")
    void payingWardLetsSpellResolve() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ThunderSalvo()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, armadillo.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(armadillo);
        harness.assertInGraveyard(player2, "Thunder Salvo");
    }

    @Test
    void toughnessIsDeterminedAtResolution() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        harness.setHand(player1, List.of(new TakeUpTheShield()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player1, 0, armadillo.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, armadillo)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, armadillo)).isEqualTo(5);
    }

    @Test
    void laterToughnessChangesDoNotRecalculateTheBoost() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        harness.setHand(player1, List.of(new TakeUpTheShield()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.castInstant(player1, 0, armadillo.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, armadillo)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, armadillo)).isEqualTo(5);
    }

    @Test
    void repeatedActivationsAccumulateWithoutIncreasingToughness() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, armadillo)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, armadillo)).isEqualTo(4);
    }

    @Test
    void wardPreventsDamageWhenPaymentIsUnavailable() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        harness.setHand(player2, List.of(new ThunderSalvo()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, armadillo.getId());
        resolveAllTriggers();

        assertThat(armadillo.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Thunder Salvo");
    }

    @Test
    void payingWardAllowsDamageToBeDealt() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        harness.setHand(player2, List.of(new ThunderSalvo()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, armadillo.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(armadillo.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningWardCountersAnOpposingActivatedAbility() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        Permanent keykeeper = addCreatureReady(player2, new SterlingKeykeeper());
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.activateAbility(player2, 0, null, armadillo.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(armadillo.isTapped()).isFalse();
        assertThat(keykeeper.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingWardAllowsAnOpposingActivatedAbilityToResolve() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        addCreatureReady(player2, new SterlingKeykeeper());
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.activateAbility(player2, 0, null, armadillo.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(armadillo.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardDoesNotTriggerForItsControllersSpell() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        harness.setHand(player1, List.of(new TakeUpTheShield()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, armadillo.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, armadillo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, armadillo)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }
}
