package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WillowSatyr.class, ArvadTheCursed.class, GrizzlyBears.class})
class WillowSatyrTest extends BaseCardTest {

    @Test
    @DisplayName("{T} gains control of a target legendary creature while Willow Satyr remains tapped")
    void gainsControlWhileTapped() {
        Permanent satyr = addCreatureReady(player1, new WillowSatyr());
        Permanent legendary = addCreatureReady(player2, new ArvadTheCursed());

        activateAbility(satyr, legendary);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(legendary);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(legendary);
        assertThat(satyr.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a nonlegendary creature")
    void cannotTargetNonlegendaryCreature() {
        Permanent satyr = addCreatureReady(player1, new WillowSatyr());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(satyr), null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("Control ends when Willow Satyr untaps")
    void controlEndsWhenSatyrUntaps() {
        Permanent satyr = addCreatureReady(player1, new WillowSatyr());
        Permanent legendary = addCreatureReady(player2, new ArvadTheCursed());

        activateAbility(satyr, legendary);

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(satyr.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(legendary);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(legendary);
    }

    @Test
    @DisplayName("Choosing not to untap Willow Satyr retains control")
    void keepingSatyrTappedRetainsControl() {
        Permanent satyr = addCreatureReady(player1, new WillowSatyr());
        Permanent legendary = addCreatureReady(player2, new ArvadTheCursed());

        activateAbility(satyr, legendary);

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(satyr.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(legendary);
    }

    private void activateAbility(Permanent satyr, Permanent target) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(satyr);
        harness.activateAbility(player1, index, null, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
