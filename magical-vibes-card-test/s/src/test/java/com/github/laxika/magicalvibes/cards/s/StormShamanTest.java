package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormShaman.class})
class StormShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Ability uses the stack and boosts only its source")
    void boostsOnlyTheActivatedShamanAfterResolution() {
        Permanent first = addCreatureReady(player1, new StormShaman());
        Permanent second = addCreatureReady(player1, new StormShaman());
        Permanent opposing = addCreatureReady(player2, new StormShaman());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isZero();

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opposing)).isZero();
    }

    @Test
    @DisplayName("Activating ability gives +1/+0")
    void activatingAbilityBoostsPower() {
        Permanent shaman = addCreatureReady(player1, new StormShaman());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, shaman)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, shaman)).isEqualTo(0);
    }

    @Test
    void requiresRedMana() {
        addCreatureReady(player1, new StormShaman());
        harness.addMana(player1, ManaColor.BLUE, 1);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate multiple times — each gives +1/+0")
    void canActivateMultipleTimes() {
        Permanent shaman = addCreatureReady(player1, new StormShaman());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(PermanentGrants.powerBoostOf(gd, shaman)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, shaman)).isEqualTo(0);
    }

    @Test
    void canBeActivatedWhileSummoningSick() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new StormShaman());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, shaman)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can be activated while tapped")
    void canBeActivatedWhileTapped() {
        Permanent shaman = addCreatureReady(player1, new StormShaman());
        shaman.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, shaman)).isEqualTo(1);
        assertThat(shaman.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent shaman = addCreatureReady(player1, new StormShaman());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, shaman)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, shaman)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, shaman)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can be activated during an opponent's turn")
    void canBeActivatedDuringOpponentsTurn() {
        Permanent shaman = addCreatureReady(player1, new StormShaman());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, shaman)).isEqualTo(1);
    }
}
