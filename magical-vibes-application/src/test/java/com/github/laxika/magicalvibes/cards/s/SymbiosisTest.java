package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Fluctuator;
import com.github.laxika.magicalvibes.cards.p.PouncingJaguar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Symbiosis.class, PouncingJaguar.class, Fluctuator.class})
class SymbiosisTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Both target creatures get +2/+2 until end of turn")
    void boostsBothTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PouncingJaguar());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new PouncingJaguar());
        harness.setHand(player1, List.of(new Symbiosis()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PouncingJaguar());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PouncingJaguar());
        harness.setHand(player1, List.of(new Symbiosis()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("Requires exactly two creature targets")
    void requiresExactlyTwoCreatureTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new PouncingJaguar());
        harness.setHand(player1, List.of(new Symbiosis()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseSameCreatureTwice() {
        Permanent jaguar = harness.addToBattlefieldAndReturn(player1, new PouncingJaguar());
        harness.setHand(player1, List.of(new Symbiosis()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(jaguar.getId(), jaguar.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Still boosts the legal target when the other target leaves before resolution")
    void boostsRemainingLegalTarget() {
        Permanent removed = harness.addToBattlefieldAndReturn(player1, new PouncingJaguar());
        Permanent surviving = harness.addToBattlefieldAndReturn(player2, new PouncingJaguar());
        harness.setHand(player1, List.of(new Symbiosis()));
        giveMana();

        harness.castInstant(player1, 0, List.of(removed.getId(), surviving.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(removed);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, surviving)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, surviving)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new PouncingJaguar());
        Permanent fluctuator = harness.addToBattlefieldAndReturn(player1, new Fluctuator());
        harness.setHand(player1, List.of(new Symbiosis()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bear.getId(), fluctuator.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot cast without targets")
    void cannotCastWithoutTargets() {
        harness.addToBattlefield(player1, new PouncingJaguar());
        harness.addToBattlefield(player2, new PouncingJaguar());
        harness.setHand(player1, List.of(new Symbiosis()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose three creatures")
    void cannotChooseThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PouncingJaguar());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PouncingJaguar());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new PouncingJaguar());
        harness.setHand(player1, List.of(new Symbiosis()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not resolve when both targets leave the battlefield")
    void doesNotResolveWhenBothTargetsLeave() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PouncingJaguar());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new PouncingJaguar());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player1, new PouncingJaguar());
        harness.setHand(player1, List.of(new Symbiosis()));
        giveMana();

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerBattlefields.get(player2.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Symbiosis");
        assertThat(gqs.getEffectivePower(gd, untargeted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, untargeted)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
    }
}
