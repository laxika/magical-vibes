package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AtarkaPummeler.class, GrizzlyBears.class})
class AtarkaPummelerTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate formidable below total power eight")
    void cannotActivateBelowTotalPowerEight() {
        Permanent pummeler = addCreatureReady(player1, new AtarkaPummeler());
        addCreatureReady(player1, new GrizzlyBears());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power");
        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Formidable grants menace to all creatures controlled by its controller")
    void grantsMenaceToControlledCreatures() {
        Permanent pummeler = addCreatureReady(player1, new AtarkaPummeler());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondBear, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Formidable menace lasts until end of turn")
    void menaceWearsOffAtEndOfTurn() {
        Permanent pummeler = addCreatureReady(player1, new AtarkaPummeler());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.MENACE)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.MENACE)).isFalse();
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
