package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlamekinBrawler.class})
class FlamekinBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +1/+0 until end of turn")
    void resolvingAbilityBoostsPower() {
        Permanent brawler = addCreatureReady(player1, new FlamekinBrawler());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, brawler)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, brawler)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate multiple times for cumulative power boost")
    void canActivateMultipleTimes() {
        Permanent brawler = addCreatureReady(player1, new FlamekinBrawler());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, brawler)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, brawler)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new FlamekinBrawler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent brawler = addCreatureReady(player1, new FlamekinBrawler());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, brawler)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, brawler)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent brawler = harness.addToBattlefieldAndReturn(player1, new FlamekinBrawler());
        brawler.setSummoningSick(true);
        brawler.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, brawler)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, brawler)).isZero();
        assertThat(brawler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost uses the stack and affects only the activating copy")
    void boostWaitsForResolutionAndOnlyAffectsSource() {
        Permanent brawler = addCreatureReady(player1, new FlamekinBrawler());
        Permanent otherBrawler = addCreatureReady(player1, new FlamekinBrawler());
        Permanent opposingBrawler = addCreatureReady(player2, new FlamekinBrawler());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(PermanentGrants.powerBoostOf(gd, brawler)).isZero();

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, brawler)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, brawler)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, otherBrawler)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opposingBrawler)).isZero();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the red activation cost")
    void cannotActivateWithOnlyColorlessMana() {
        addCreatureReady(player1, new FlamekinBrawler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
    }
}
