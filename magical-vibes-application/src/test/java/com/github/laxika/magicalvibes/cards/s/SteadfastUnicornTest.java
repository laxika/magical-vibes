package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteadfastUnicorn.class, GrizzlyBears.class})
class SteadfastUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts creatures you control and grants them vigilance")
    void boostsOwnCreaturesAndGrantsVigilance() {
        Permanent unicorn = addCreatureReady(player1, new SteadfastUnicorn());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        prepareAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, unicorn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unicorn)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, unicorn, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The boost and vigilance wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new SteadfastUnicorn());
        prepareAbility();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot be activated during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new SteadfastUnicorn());
        addMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareAbility() {
        addMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
