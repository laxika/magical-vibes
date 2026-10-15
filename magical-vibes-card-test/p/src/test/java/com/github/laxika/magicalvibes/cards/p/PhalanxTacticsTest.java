package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.f.FinalDeath;
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

@CardUsed({PhalanxTactics.class, NyxbornCourser.class, FinalDeath.class})
class PhalanxTacticsTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the target creature by an additional +1/+0")
    void boostsTargetAndOtherControlledCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        castTactics(target);

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(other.getEffectivePower()).isEqualTo(3);
        assertThat(other.getEffectiveToughness()).isEqualTo(5);
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boosts wear off at end of turn")
    void boostsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        castTactics(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.setHand(player1, List.of(new PhalanxTactics()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No creatures are boosted when the only target leaves before resolution")
    void doesNotBoostOtherCreaturesWhenTargetIsExiled() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        beginCastingTactics(target);

        harness.setHand(player2, List.of(new FinalDeath()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Phalanx Tactics");
    }

    @Test
    @DisplayName("Creatures entering before resolution receive the other-creature boost")
    void boostsCreatureEnteringBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        beginCastingTactics(target);
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(newcomer.getEffectivePower()).isEqualTo(3);
        assertThat(newcomer.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Creatures entering after resolution receive no boost")
    void doesNotBoostCreatureEnteringAfterResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        castTactics(target);

        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(newcomer.getEffectivePower()).isEqualTo(2);
        assertThat(newcomer.getEffectiveToughness()).isEqualTo(4);
    }

    private void castTactics(Permanent target) {
        beginCastingTactics(target);
        harness.passBothPriorities();
    }

    private void beginCastingTactics(Permanent target) {
        harness.setHand(player1, List.of(new PhalanxTactics()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
    }
}
