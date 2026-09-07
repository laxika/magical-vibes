package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcefallRegent.class, GrizzlyBears.class, LightningBolt.class})
class IcefallRegentTest extends BaseCardTest {

    @Test
    @DisplayName("When Icefall Regent enters, it taps and locks an opponent's creature")
    void entersTapsAndLocksOpponentCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castAndResolveRegent(bears);

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getUntapPreventedWhileSourceOnBattlefieldIds()).isNotEmpty();
    }

    @Test
    @DisplayName("The untap lock ends when Icefall Regent leaves the battlefield")
    void lockEndsWhenRegentLeavesBattlefield() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castAndResolveRegent(bears);
        Permanent regent = findPermanent(player1, "Icefall Regent");

        advanceToNextTurn(player1);
        assertThat(bears.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(regent);
        advanceToNextTurn(player2);
        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Icefall Regent can target only an opponent's creature")
    void canTargetOnlyOpponentCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IcefallRegent()));
        addRegentMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's spell targeting Icefall Regent costs {2} more")
    void opponentSpellTargetingRegentCostsMore() {
        Permanent regent = addCreatureReady(player1, new IcefallRegent());
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, regent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay targeting tax");
    }

    private void castAndResolveRegent(Permanent target) {
        harness.setHand(player1, List.of(new IcefallRegent()));
        addRegentMana();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addRegentMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
