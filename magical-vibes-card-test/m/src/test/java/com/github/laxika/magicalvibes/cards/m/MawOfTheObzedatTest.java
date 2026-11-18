package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MawOfTheObzedat.class, KraulWarrior.class})
class MawOfTheObzedatTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature gives creatures you control +1/+1")
    void boostsOwnCreatures() {
        Permanent maw = harness.addToBattlefieldAndReturn(player1, new MawOfTheObzedat());
        Permanent food = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Kraul Warrior");
        assertThat(gqs.getEffectivePower(gd, maw)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, maw)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost the opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefieldAndReturn(player1, new MawOfTheObzedat());
        Permanent food = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new KraulWarrior());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, theirs)).isEqualTo(2);
    }

    @Test
    @DisplayName("Maw can eat itself, still boosting the rest of the team")
    void canSacrificeItself() {
        Permanent maw = harness.addToBattlefieldAndReturn(player1, new MawOfTheObzedat());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, maw.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Maw of the Obzedat");
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        Permanent maw = harness.addToBattlefieldAndReturn(player1, new MawOfTheObzedat());
        Permanent food = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, maw)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, maw)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, but the boost waits for resolution")
    void sacrificeIsACost() {
        Permanent maw = harness.addToBattlefieldAndReturn(player1, new MawOfTheObzedat());
        Permanent food = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, food.getId());

        harness.assertInGraveyard(player1, "Kraul Warrior");
        harness.assertNotOnBattlefield(player1, "Kraul Warrior");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, maw)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, maw)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, maw)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, maw)).isEqualTo(4);
    }

    @Test
    @DisplayName("Repeated activations stack even when Maw sacrifices itself on the second")
    void repeatedActivationsSurviveSourceLeaving() {
        Permanent maw = harness.addToBattlefieldAndReturn(player1, new MawOfTheObzedat());
        Permanent food = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, food.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, maw.getId());

        harness.assertInGraveyard(player1, "Maw of the Obzedat");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(4);
    }

    @Test
    @DisplayName("Only creatures present when the ability resolves receive the boost")
    void affectedCreaturesAreDeterminedAtResolution() {
        harness.addToBattlefield(player1, new MawOfTheObzedat());
        Permanent food = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, food.getId());
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new KraulWarrior());
        harness.passBothPriorities();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new KraulWarrior());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
    }
}
