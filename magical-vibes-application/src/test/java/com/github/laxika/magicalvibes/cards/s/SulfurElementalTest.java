package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
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

@CardUsed({SulfurElemental.class, WhiteKnight.class, GrizzlyBears.class, Shock.class})
class SulfurElementalTest extends BaseCardTest {

    @Test
    @DisplayName("White creatures get +1/-1")
    void buffsAndDebuffsWhiteCreatures() {
        harness.addToBattlefield(player1, new SulfurElemental());
        harness.addToBattlefield(player1, new WhiteKnight());
        harness.addToBattlefield(player2, new WhiteKnight());

        Permanent whiteKnight = findPermanent(player1, "White Knight");
        Permanent opponentWhiteKnight = findPermanent(player2, "White Knight");

        assertThat(gqs.getEffectivePower(gd, whiteKnight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, whiteKnight)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentWhiteKnight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentWhiteKnight)).isEqualTo(1);
    }

    @Test
    @DisplayName("Nonwhite creatures are unaffected")
    void ignoresNonwhiteCreatures() {
        harness.addToBattlefield(player1, new SulfurElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be cast during the opponent's turn")
    void canBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SulfurElemental()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.getGameService().passPriority(harness.getGameData(), player2);
        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Split second prevents an opponent's spell response")
    void splitSecondPreventsSpellResponse() {
        harness.setHand(player1, List.of(new SulfurElemental()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
