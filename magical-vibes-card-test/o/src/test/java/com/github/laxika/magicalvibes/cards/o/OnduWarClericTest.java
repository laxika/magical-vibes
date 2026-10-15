package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OnduWarCleric.class})
class OnduWarClericTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort taps an Ally and gains 2 life")
    void cohortTapsAnAllyAndGainsLife() {
        Permanent cleric = addCreatureReady(player1, new OnduWarCleric());
        Permanent ally = addCreatureReady(player1, new OnduWarCleric());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, battlefieldIndex(cleric), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(cleric.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cohort cannot be activated without another untapped Ally")
    void cannotActivateWithoutAnotherUntappedAlly() {
        Permanent cleric = addCreatureReady(player1, new OnduWarCleric());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(cleric), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    void summoningSickAllyCanPayTheAdditionalTapCost() {
        Permanent cleric = addCreatureReady(player1, new OnduWarCleric());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new OnduWarCleric());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, battlefieldIndex(cleric), 0, null, null);

        assertThat(cleric.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        harness.assertLife(player1, lifeBefore);

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 2);
        harness.assertLife(player2, 20);
    }

    @Test
    void summoningSickClericCannotActivateCohort() {
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new OnduWarCleric());
        Permanent ally = addCreatureReady(player1, new OnduWarCleric());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(cleric), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(cleric.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedAllyCannotPayCohortCost() {
        Permanent cleric = addCreatureReady(player1, new OnduWarCleric());
        Permanent ally = addCreatureReady(player1, new OnduWarCleric());
        ally.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(cleric), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(cleric.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentAllyCannotPayCohortCost() {
        Permanent cleric = addCreatureReady(player1, new OnduWarCleric());
        Permanent ally = addCreatureReady(player2, new OnduWarCleric());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(cleric), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(cleric.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedClericCannotActivateCohort() {
        Permanent cleric = addCreatureReady(player1, new OnduWarCleric());
        Permanent ally = addCreatureReady(player1, new OnduWarCleric());
        cleric.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(cleric), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(ally.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cohortStillResolvesAfterBothAlliesLeaveBattlefield() {
        Permanent cleric = addCreatureReady(player1, new OnduWarCleric());
        Permanent ally = addCreatureReady(player1, new OnduWarCleric());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, battlefieldIndex(cleric), 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(cleric);
        gd.playerBattlefields.get(player1.getId()).remove(ally);
        gd.playerGraveyards.get(player1.getId()).add(cleric.getCard());
        gd.playerGraveyards.get(player1.getId()).add(ally.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 2);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
