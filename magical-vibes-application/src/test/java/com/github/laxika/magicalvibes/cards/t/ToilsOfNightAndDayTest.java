package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToilsOfNightAndDay.class, GnarledMass.class, TendoIceBridge.class})
class ToilsOfNightAndDayTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting both optional clauses affects each target independently")
    void affectsBothTargetsIndependently() {
        Permanent bears = addCreatureReady(player2, new GnarledMass());
        Permanent bridge = harness.addToBattlefieldAndReturn(player1, new TendoIceBridge());
        bridge.tap();

        castToils(bears, bridge);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isTrue();
        assertThat(bridge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the first prompt leaves that permanent untouched")
    void decliningFirstOnlyAffectsSecond() {
        Permanent bears = addCreatureReady(player2, new GnarledMass());
        Permanent bridge = harness.addToBattlefieldAndReturn(player1, new TendoIceBridge());
        bridge.tap();

        castToils(bears, bridge);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isFalse();
        assertThat(bridge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining both prompts changes nothing")
    void decliningBothDoesNothing() {
        Permanent bears = addCreatureReady(player2, new GnarledMass());
        Permanent bridge = harness.addToBattlefieldAndReturn(player1, new TendoIceBridge());
        bridge.tap();

        castToils(bears, bridge);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isFalse();
        assertThat(bridge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting the first prompt and declining the second only affects the first target")
    void decliningSecondOnlyAffectsFirst() {
        Permanent bears = addCreatureReady(player2, new GnarledMass());
        Permanent bridge = harness.addToBattlefieldAndReturn(player1, new TendoIceBridge());
        bridge.tap();

        castToils(bears, bridge);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isTrue();
        assertThat(bridge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An accepted clause untaps an already-tapped target")
    void acceptedClauseUntapsTappedTarget() {
        Permanent bears = addCreatureReady(player2, new GnarledMass());
        Permanent bridge = harness.addToBattlefieldAndReturn(player1, new TendoIceBridge());
        bridge.tap();

        castToils(bears, bridge);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bridge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The second clause still resolves if the first target leaves before resolution")
    void resolvesSecondClauseWhenFirstTargetLeaves() {
        Permanent bears = addCreatureReady(player2, new GnarledMass());
        Permanent bridge = harness.addToBattlefieldAndReturn(player1, new TendoIceBridge());
        bridge.tap();

        prepareCast();
        harness.castInstant(player1, 0, List.of(bears.getId(), bridge.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(bridge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The second target must be another permanent")
    void rejectsTheSamePermanentTwice() {
        Permanent bears = addCreatureReady(player2, new GnarledMass());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castToils(Permanent first, Permanent second) {
        prepareCast();
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ToilsOfNightAndDay()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
