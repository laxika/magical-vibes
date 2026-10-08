package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarScreecher.class, GrizzlyBears.class})
class WarScreecherTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other creatures you control, but not itself or an opponent's creature")
    void boostsOtherCreaturesYouControl() {
        Permanent screecher = addCreatureReady(player1, new WarScreecher());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(screecher), null, null);
        harness.passBothPriorities();

        assertThat(screecher.getEffectivePower()).isEqualTo(1);
        assertThat(screecher.getEffectiveToughness()).isEqualTo(3);
        assertThat(ally.getEffectivePower()).isEqualTo(3);
        assertThat(ally.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(2);
        assertThat(screecher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent screecher = addCreatureReady(player1, new WarScreecher());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(screecher), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ally.getEffectivePower()).isEqualTo(2);
        assertThat(ally.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent screecher = addCreatureReady(player1, new WarScreecher());
        screecher.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(screecher), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Another War Screecher entering before resolution receives the boost")
    void boostsAnotherScreecherPresentAtResolution() {
        Permanent source = addCreatureReady(player1, new WarScreecher());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new WarScreecher());
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(1);
        assertThat(source.getEffectiveToughness()).isEqualTo(3);
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void doesNotBoostCreaturesEnteringAfterResolution() {
        addCreatureReady(player1, new WarScreecher());
        Permanent other = addCreatureReady(player1, new WarScreecher());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new WarScreecher());

        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(4);
        assertThat(lateCreature.getEffectivePower()).isEqualTo(1);
        assertThat(lateCreature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new WarScreecher());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
