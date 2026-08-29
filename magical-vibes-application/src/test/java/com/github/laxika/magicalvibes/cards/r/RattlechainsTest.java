package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpectralShepherd;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RattlechainsTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast a Spirit spell during an opponent's turn")
    void canCastSpiritSpellDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new Rattlechains());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Rattlechains());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Rattlechains()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        harness.castCreature(player1, 0, 0, target.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-Spirit creature spells cannot use Rattlechains's flash permission")
    void cannotCastNonSpiritSpellDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new Rattlechains());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Rattlechains gives the targeted Spirit hexproof until end of turn")
    void grantsHexproofUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpectralShepherd());
        harness.setHand(player1, List.of(new Rattlechains()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).contains(Keyword.HEXPROOF);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Rattlechains cannot target a non-Spirit permanent")
    void cannotTargetNonSpirit() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Rattlechains()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Spirit");
    }
}
