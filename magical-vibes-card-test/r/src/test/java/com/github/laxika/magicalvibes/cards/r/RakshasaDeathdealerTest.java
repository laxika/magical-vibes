package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MurderousCut;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakshasaDeathdealer.class, MurderousCut.class})
class RakshasaDeathdealerTest extends BaseCardTest {

    @Test
    @DisplayName("{B}{G}: gets +2/+2 until end of turn")
    void pumpAbilityBoostsPowerAndToughness() {
        Permanent deathdealer = addCreatureReady(player1, new RakshasaDeathdealer());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(deathdealer.getPowerModifier()).isEqualTo(2);
        assertThat(deathdealer.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("{B}{G}: regenerates")
    void regenerationAbilityGrantsShield() {
        Permanent deathdealer = addCreatureReady(player1, new RakshasaDeathdealer());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(deathdealer.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Power and toughness boost wears off at end of turn")
    void pumpAbilityWearsOffAtEndOfTurn() {
        Permanent deathdealer = addCreatureReady(player1, new RakshasaDeathdealer());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(deathdealer.getPowerModifier()).isEqualTo(0);
        assertThat(deathdealer.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void pumpActivationsStackEvenWhileTappedAndSummoningSick() {
        Permanent deathdealer = new Permanent(new RakshasaDeathdealer());
        deathdealer.setSummoningSick(true);
        deathdealer.tap();
        gd.playerBattlefields.get(player1.getId()).add(deathdealer);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(deathdealer.getPowerModifier()).isEqualTo(4);
        assertThat(deathdealer.getToughnessModifier()).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void regenerationInResponsePreventsDestructionAndConsumesOnlyOneShield() {
        Permanent deathdealer = addCreatureReady(player1, new RakshasaDeathdealer());
        harness.setHand(player2, List.of(new MurderousCut()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player2, 0, deathdealer.getId());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Rakshasa Deathdealer");
        harness.assertNotInGraveyard(player1, "Rakshasa Deathdealer");
        assertThat(deathdealer.isTapped()).isTrue();
        assertThat(deathdealer.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void unusedRegenerationShieldExpiresAtEndOfTurn() {
        Permanent deathdealer = addCreatureReady(player1, new RakshasaDeathdealer());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(deathdealer.isTapped()).isFalse();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(deathdealer.getRegenerationShield()).isZero();
    }
}
