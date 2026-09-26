package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarVanguard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreakingWave.class, LlanowarVanguard.class, Forest.class})
class BreakingWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Simultaneously flips the tap states of creatures on every battlefield")
    void flipsAllCreatureTapStates() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarVanguard());
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarVanguard());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarVanguard());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Forest());
        tappedCreature.tap();
        noncreature.tap();

        harness.setHand(player1, List.of(new BreakingWave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(untappedCreature.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(noncreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can be cast at instant speed by paying two more")
    void canBeCastAtInstantSpeedForTwoMore() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LlanowarVanguard());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BreakingWave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot be cast at instant speed without the surcharge")
    void cannotBeCastAtInstantSpeedWithoutSurcharge() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BreakingWave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
