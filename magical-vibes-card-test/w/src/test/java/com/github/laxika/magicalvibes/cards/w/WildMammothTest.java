package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.StampedeDriver;
import com.github.laxika.magicalvibes.cards.p.ParallaxWave;
import com.github.laxika.magicalvibes.cards.s.SealOfCleansing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildMammoth.class, StampedeDriver.class, ParallaxWave.class, SealOfCleansing.class})
class WildMammothTest extends BaseCardTest {

    @Test
    @DisplayName("The player with the most creatures gains control during upkeep")
    void playerWithMostCreaturesGainsControl() {
        harness.addToBattlefield(player1, new WildMammoth());
        harness.addToBattlefield(player2, new StampedeDriver());
        harness.addToBattlefield(player2, new StampedeDriver());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wild Mammoth");
        harness.assertOnBattlefield(player2, "Wild Mammoth");
    }

    @Test
    @DisplayName("The controller keeps Wild Mammoth when they control the most creatures")
    void controllerKeepsWhenTheyHaveMostCreatures() {
        harness.addToBattlefield(player1, new WildMammoth());
        harness.addToBattlefield(player1, new StampedeDriver());
        harness.addToBattlefield(player2, new StampedeDriver());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wild Mammoth");
        harness.assertNotOnBattlefield(player2, "Wild Mammoth");
    }

    @Test
    @DisplayName("Wild Mammoth does not change control when creature counts are tied")
    void noChangeOnTie() {
        harness.addToBattlefield(player1, new WildMammoth());
        harness.addToBattlefield(player2, new StampedeDriver());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wild Mammoth");
        harness.assertNotOnBattlefield(player2, "Wild Mammoth");
    }

    @Test
    @DisplayName("The creature-count condition is checked again when the ability resolves")
    void doesNothingIfCreatureCountsAreNoLongerUniqueAtResolution() {
        harness.addToBattlefield(player1, new WildMammoth());
        harness.addToBattlefield(player2, new StampedeDriver());
        harness.addToBattlefield(player2, new StampedeDriver());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new StampedeDriver());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wild Mammoth");
        harness.assertNotOnBattlefield(player2, "Wild Mammoth");
    }

    @Test
    @DisplayName("Wild Mammoth does not trigger during its opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new WildMammoth());
        harness.addToBattlefield(player2, new StampedeDriver());
        harness.addToBattlefield(player2, new StampedeDriver());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Wild Mammoth");
    }

    @Test
    @DisplayName("A tie at the beginning of upkeep prevents a trigger even if the tie later breaks")
    void tieAtTriggerTimeDoesNotCreatePendingAbility() {
        harness.addToBattlefield(player1, new WildMammoth());
        harness.addToBattlefield(player2, new StampedeDriver());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player2, new StampedeDriver());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Wild Mammoth");
        harness.assertNotOnBattlefield(player2, "Wild Mammoth");
    }

    @Test
    @DisplayName("The pending upkeep ability cannot affect Wild Mammoth after it leaves and returns")
    void pendingAbilityDoesNotAffectReturnedMammoth() {
        Permanent mammoth = harness.addToBattlefieldAndReturn(player1, new WildMammoth());
        harness.addToBattlefield(player1, new SealOfCleansing());
        harness.addToBattlefield(player2, new StampedeDriver());
        harness.addToBattlefield(player2, new StampedeDriver());
        Permanent wave = harness.addToBattlefieldAndReturn(player2, new ParallaxWave());
        wave.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player2, 2, null, mammoth.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Wild Mammoth");
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, null, wave.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returnedMammoth = findPermanent(player1, "Wild Mammoth");
        assertThat(returnedMammoth.getId()).isNotEqualTo(mammoth.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Wild Mammoth");
        harness.assertNotOnBattlefield(player2, "Wild Mammoth");
    }
}
