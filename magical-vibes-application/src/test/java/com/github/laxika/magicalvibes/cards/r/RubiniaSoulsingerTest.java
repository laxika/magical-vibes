package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.p.Pendelhaven;
import com.github.laxika.magicalvibes.cards.t.Twiddle;
import com.github.laxika.magicalvibes.cards.w.WillowSatyr;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RubiniaSoulsinger.class, BarbaryApes.class, Pendelhaven.class, WillowSatyr.class, Boomerang.class,
        Twiddle.class})
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

    @Test
    @DisplayName("Control ends when Rubinia leaves the battlefield")
    void controlEndsWhenRubiniaLeavesBattlefield() {
        Permanent rubinia = addCreatureReady(player1, new RubiniaSoulsinger());
        Permanent apes = addCreatureReady(player2, new BarbaryApes());

        harness.activateAbility(player1, 0, null, apes.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(apes);

        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, rubinia.getId());

        harness.assertInHand(player1, "Rubinia Soulsinger");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(apes);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rubinia, apes);
    }

    @Test
    @DisplayName("No control is gained if Rubinia leaves before her ability resolves")
    void sourceLeavingBeforeResolutionPreventsControl() {
        Permanent rubinia = addCreatureReady(player1, new RubiniaSoulsinger());
        Permanent apes = addCreatureReady(player2, new BarbaryApes());

        harness.activateAbility(player1, 0, null, apes.getId());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, rubinia.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Rubinia Soulsinger");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(apes);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(apes);
    }

    @Test
    @DisplayName("No control is gained if Rubinia changes controllers before resolution")
    void sourceChangingControllersBeforeResolutionPreventsControl() {
        Permanent rubinia = addCreatureReady(player1, new RubiniaSoulsinger());
        Permanent apes = addCreatureReady(player2, new BarbaryApes());
        Permanent satyr = addCreatureReady(player2, new WillowSatyr());

        harness.activateAbility(player1, 0, null, apes.getId());
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(satyr),
                null, rubinia.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(rubinia);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(rubinia, apes);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rubinia, apes);
    }

    @Test
    @DisplayName("A creature returned to hand in response is not gained")
    void targetLeavingBeforeResolutionIsNotGained() {
        Permanent rubinia = addCreatureReady(player1, new RubiniaSoulsinger());
        Permanent apes = addCreatureReady(player2, new BarbaryApes());

        harness.activateAbility(player1, 0, null, apes.getId());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, apes.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Barbary Apes");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rubinia).doesNotContain(apes);
        assertThat(rubinia.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapping and retapping Rubinia before resolution does not restore the duration")
    void untappingAndRetappingBeforeResolutionPreventsControl() {
        Permanent rubinia = addCreatureReady(player1, new RubiniaSoulsinger());
        Permanent apes = addCreatureReady(player2, new BarbaryApes());

        harness.activateAbility(player1, 0, null, apes.getId());
        harness.setHand(player2, List.of(new Twiddle(), new Twiddle()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, rubinia.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(rubinia.isTapped()).isFalse();

        harness.castAndResolveInstant(player2, 0, rubinia.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(rubinia.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(apes);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rubinia).doesNotContain(apes);
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
