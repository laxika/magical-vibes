package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.t.TombOfUrami;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkkiDrillmaster.class, TombOfUrami.class})
class AkkiDrillmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability grants haste to target creature")
    void resolvingGrantsHaste() {
        addCreatureReady(player1, new AkkiDrillmaster());
        Permanent target = addCreatureReady(player1, new AkkiDrillmaster());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentCreature() {
        addCreatureReady(player1, new AkkiDrillmaster());
        Permanent target = addCreatureReady(player2, new AkkiDrillmaster());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste is removed at end of turn")
    void hasteRemovedAtEndOfTurn() {
        addCreatureReady(player1, new AkkiDrillmaster());
        Permanent target = addCreatureReady(player1, new AkkiDrillmaster());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new AkkiDrillmaster());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TombOfUrami());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Activating the ability taps Akki Drillmaster")
    void activationTapsSource() {
        Permanent source = addCreatureReady(player1, new AkkiDrillmaster());
        Permanent target = addCreatureReady(player1, new AkkiDrillmaster());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
    }
}
