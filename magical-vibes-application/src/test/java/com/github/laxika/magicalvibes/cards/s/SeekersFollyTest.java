package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeekersFollyTest extends BaseCardTest {

    @Test
    void opponentDiscardModeDiscardsTwoCards() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setHand(player1, List.of(new SeekersFolly()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void discardModeCannotTargetController() {
        harness.setHand(player1, List.of(new SeekersFolly()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentCreatureModeDebuffsOnlyOpponentsUntilEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeekersFolly()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getEffectiveToughness()).isEqualTo(2);
    }
}
