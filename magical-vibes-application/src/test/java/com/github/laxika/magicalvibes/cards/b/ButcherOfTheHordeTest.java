package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ButcherOfTheHorde.class, AlpineGrizzly.class})
class ButcherOfTheHordeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature grants vigilance")
    void grantsVigilance() {
        Permanent butcher = addButcherReady(player1);
        addCreatureReady(player1);

        activateAndSacrifice();
        harness.handleListChoice(player1, "Vigilance");

        assertThat(gqs.hasKeyword(gd, butcher, Keyword.VIGILANCE)).isTrue();
        harness.assertInGraveyard(player1, "Alpine Grizzly");
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

    @Test
    @DisplayName("The sacrifice is paid before resolution and grants only the chosen keyword")
    void sacrificeIsPaidBeforeResolution() {
        Permanent butcher = addButcherReady(player1);
        addCreatureReady(player1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Alpine Grizzly");
        assertThat(gqs.hasKeyword(gd, butcher, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, butcher, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, butcher, Keyword.HASTE)).isFalse();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Vigilance");

        assertThat(gqs.hasKeyword(gd, butcher, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, butcher, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, butcher, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addButcherReady(player1);
        Permanent opponentCreature = addCreatureReady(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        harness.assertNotInGraveyard(player2, "Alpine Grizzly");
    }

    @Test
    @DisplayName("Repeated activations accumulate different keywords until cleanup")
    void repeatedActivationsAccumulateKeywords() {
        Permanent butcher = addButcherReady(player1);
        butcher.setTapped(true);
        butcher.setSummoningSick(true);
        addCreatureReady(player1);

        activateAndSacrifice();
        harness.handleListChoice(player1, "Vigilance");
        addCreatureReady(player1);
        activateAndSacrifice();
        harness.handleListChoice(player1, "Lifelink");
        addCreatureReady(player1);
        activateAndSacrifice();
        harness.handleListChoice(player1, "Haste");

        assertThat(gqs.hasKeyword(gd, butcher, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, butcher, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, butcher, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, butcher, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, butcher, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, butcher, Keyword.HASTE)).isFalse();
    }

    private Permanent addButcherReady(Player player) {
        return addCreatureReady(player, new ButcherOfTheHorde());
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new AlpineGrizzly());
    }

    private void activateAndSacrifice() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
