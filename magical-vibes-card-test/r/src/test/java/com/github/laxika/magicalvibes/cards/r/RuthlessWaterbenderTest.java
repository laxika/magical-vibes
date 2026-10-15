package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuthlessWaterbender.class})
class RuthlessWaterbenderTest extends BaseCardTest {

    @Test
    @DisplayName("Waterbend taps artifacts or creatures and boosts this creature until end of turn")
    void waterbendBoostsThisCreatureUntilEndOfTurn() {
        Permanent waterbender = harness.addToBattlefieldAndReturn(player1, new RuthlessWaterbender());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuthlessWaterbender());

        harness.activateAbility(player1, 0, null, null);

        assertThat(waterbender.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(4);

        harness.passUntilWithNoAttackers(player1, TurnStep.UNTAP);

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(3);
    }

    @Test
    @DisplayName("Waterbend can be activated only during your turn")
    void waterbendRequiresYourTurn() {
        harness.addToBattlefieldAndReturn(player1, new RuthlessWaterbender());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Waterbend can be paid entirely with mana while the source is tapped")
    void paysWithManaWhileTapped() {
        Permanent waterbender = harness.addToBattlefieldAndReturn(player1, new RuthlessWaterbender());
        waterbender.tap();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(4);
        assertThat(waterbender.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Waterbend can combine mana and tapping a summoning-sick source")
    void combinesManaAndSummoningSickCreature() {
        Permanent waterbender = harness.addToBattlefieldAndReturn(player1, new RuthlessWaterbender());
        waterbender.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(waterbender.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(4);
    }

    @Test
    @DisplayName("Waterbend cannot use an opponent's creature to complete payment")
    void cannotTapOpponentCreature() {
        Permanent waterbender = harness.addToBattlefieldAndReturn(player1, new RuthlessWaterbender());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new RuthlessWaterbender());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("waterbend cost");

        assertThat(waterbender.isTapped()).isFalse();
        assertThat(opponent.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Repeated activations during your end step accumulate until cleanup")
    void repeatedEndStepActivationsAccumulate() {
        Permanent waterbender = harness.addToBattlefieldAndReturn(player1, new RuthlessWaterbender());
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(5);

        harness.passUntil(TurnStep.UNTAP);

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(3);
    }
}
