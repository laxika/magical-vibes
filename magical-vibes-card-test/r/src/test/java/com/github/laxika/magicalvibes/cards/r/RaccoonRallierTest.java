package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaccoonRallier.class, GrizzlyBears.class})
class RaccoonRallierTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature you control gains haste until end of turn")
    void grantsHasteToCreatureYouControl() {
        addReadyRallier();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Granted haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        addReadyRallier();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Ability cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        addReadyRallier();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can only be activated at sorcery speed")
    void cannotActivateOnOpponentsTurn() {
        addReadyRallier();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadyRallier() {
        return addCreatureReady(player1, new RaccoonRallier());
    }

    @Test
    void tapsSourceAsCostAndCanTargetItself() {
        Permanent rallier = addReadyRallier();

        harness.activateAbility(player1, 0, 0, null, rallier.getId());

        assertThat(rallier.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, rallier, Keyword.HASTE)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, rallier, Keyword.HASTE)).isTrue();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent rallier = addReadyRallier();
        rallier.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, rallier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }

    @Test
    void summoningSickRallierCannotGiveItselfHaste() {
        Permanent rallier = addReadyRallier();
        rallier.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, rallier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void grantedHasteLetsNewRallierActivateItsTapAbility() {
        addReadyRallier();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RaccoonRallier());
        target.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent rallier = addReadyRallier();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, rallier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotActivateWithAnAbilityOnTheStack() {
        Permanent first = addReadyRallier();
        Permanent second = addReadyRallier();
        harness.activateAbility(player1, 0, 0, null, first.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, second.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }
}
