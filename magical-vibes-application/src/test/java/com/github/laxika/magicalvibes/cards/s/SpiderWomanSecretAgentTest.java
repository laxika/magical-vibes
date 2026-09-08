package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiderWomanSecretAgent.class, GrizzlyBears.class})
class SpiderWomanSecretAgentTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps an opponent's creature and prevents it from untapping")
    void entersTapsAndLocksOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSpiderWoman(bears.getId());

        assertThat(bears.isTapped()).isTrue();
        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap lock ends when Spider-Woman leaves the battlefield")
    void untapLockEndsWhenSourceLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSpiderWoman(bears.getId());
        Permanent spiderWoman = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SpiderWomanSecretAgent)
                .findFirst()
                .orElseThrow();

        gd.playerBattlefields.get(player1.getId()).remove(spiderWoman);
        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpiderWomanSecretAgent()));
        addManaForSpiderWoman();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBears.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSpiderWoman(UUID targetId) {
        harness.setHand(player1, List.of(new SpiderWomanSecretAgent()));
        addManaForSpiderWoman();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addManaForSpiderWoman() {
        harness.addMana(player1, ManaColor.WHITE, 1);
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
