package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoughshodDuo.class, GrizzlyBears.class, Shock.class})
class RoughshodDuoTest extends BaseCardTest {

    @Test
    @DisplayName("Expend 4 boosts a target creature you control and gives it trample")
    void expendFourBoostsTargetCreatureAndGrantsTrample() {
        harness.addToBattlefield(player1, new RoughshodDuo());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFourShocks();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Roughshod Duo does not trigger before its controller expends 4")
    void doesNotTriggerBelowExpendThreshold() {
        harness.addToBattlefield(player1, new RoughshodDuo());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Roughshod Duo cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new RoughshodDuo());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFourShocks();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void boostAndTrampleWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new RoughshodDuo());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFourShocks();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    private void castFourShocks() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.castInstant(player1, 0, player2.getId());
            if (i < 3) {
                harness.passBothPriorities();
            }
        }
    }
}
