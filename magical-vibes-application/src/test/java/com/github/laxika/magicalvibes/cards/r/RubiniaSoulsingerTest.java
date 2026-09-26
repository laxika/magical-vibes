package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.p.Pendelhaven;
import com.github.laxika.magicalvibes.cards.w.WillowSatyr;
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

@CardUsed({RubiniaSoulsinger.class, BarbaryApes.class, Pendelhaven.class, WillowSatyr.class})
class RubiniaSoulsingerTest extends BaseCardTest {

    @Test
    @DisplayName("{T} gains control of a target creature while Rubinia remains tapped")
    void gainsControlWhileTapped() {
        Permanent rubinia = addCreatureReady(player1, new RubiniaSoulsinger());
        Permanent apes = addCreatureReady(player2, new BarbaryApes());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(rubinia);
        harness.activateAbility(player1, idx, null, apes.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(apes.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(apes.getId()));
        assertThat(rubinia.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent rubinia = addCreatureReady(player1, new RubiniaSoulsinger());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Pendelhaven());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(rubinia);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Control ends when Rubinia untaps")
    void controlEndsWhenRubiniaUntaps() {
        Permanent rubinia = addCreatureReady(player1, new RubiniaSoulsinger());
        Permanent apes = addCreatureReady(player2, new BarbaryApes());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(rubinia);
        harness.activateAbility(player1, idx, null, apes.getId());
        harness.passBothPriorities();

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(rubinia.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(apes.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(apes.getId()));
    }

    @Test
    @DisplayName("Choosing not to untap Rubinia retains control")
    void keepingRubiniaTappedRetainsControl() {
        Permanent rubinia = addCreatureReady(player1, new RubiniaSoulsinger());
        Permanent apes = addCreatureReady(player2, new BarbaryApes());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(rubinia);
        harness.activateAbility(player1, idx, null, apes.getId());
        harness.passBothPriorities();

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(rubinia.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(apes.getId()));
    }

    @Test
    @DisplayName("Control ends when Rubinia changes controllers")
    void controlEndsWhenRubiniaChangesControllers() {
        Permanent rubinia = addCreatureReady(player1, new RubiniaSoulsinger());
        Permanent apes = addCreatureReady(player2, new BarbaryApes());
        Permanent satyr = addCreatureReady(player2, new WillowSatyr());

        int rubiniaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(rubinia);
        harness.activateAbility(player1, rubiniaIndex, null, apes.getId());
        harness.passBothPriorities();

        int satyrIndex = gd.playerBattlefields.get(player2.getId()).indexOf(satyr);
        harness.activateAbility(player2, satyrIndex, null, rubinia.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(rubinia, apes);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rubinia, apes);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(newActivePlayer, TurnStep.PRECOMBAT_MAIN);
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
