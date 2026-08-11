package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ButcherOfTheHordeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature grants vigilance")
    void grantsVigilance() {
        Permanent butcher = addButcherReady(player1);
        addCreatureReady(player1);

        activateAndSacrifice();
        harness.handleListChoice(player1, "Vigilance");

        assertThat(gqs.hasKeyword(gd, butcher, Keyword.VIGILANCE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing another creature grants lifelink")
    void grantsLifelink() {
        Permanent butcher = addButcherReady(player1);
        addCreatureReady(player1);

        activateAndSacrifice();
        harness.handleListChoice(player1, "Lifelink");

        assertThat(gqs.hasKeyword(gd, butcher, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing another creature grants haste")
    void grantsHaste() {
        Permanent butcher = addButcherReady(player1);
        addCreatureReady(player1);

        activateAndSacrifice();
        harness.handleListChoice(player1, "Haste");

        assertThat(gqs.hasKeyword(gd, butcher, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The chosen keyword wears off during cleanup")
    void chosenKeywordWearsOffAtEndOfTurn() {
        Permanent butcher = addButcherReady(player1);
        addCreatureReady(player1);

        activateAndSacrifice();
        harness.handleListChoice(player1, "Haste");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, butcher, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The Butcher cannot sacrifice itself")
    void cannotSacrificeItself() {
        Permanent butcher = addButcherReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(butcher);
    }

    private Permanent addButcherReady(Player player) {
        return addCreatureReady(player, new ButcherOfTheHorde());
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void activateAndSacrifice() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
