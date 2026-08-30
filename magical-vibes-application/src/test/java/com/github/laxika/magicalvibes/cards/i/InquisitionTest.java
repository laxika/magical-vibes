package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Inquisition.class, GrizzlyBears.class, SavannahLions.class})
class InquisitionTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of white cards in the target player's hand")
    void dealsDamageForWhiteCardsInHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Inquisition()));
        harness.setHand(player2, List.of(new SavannahLions(), new GrizzlyBears(), new SavannahLions()));
        addInquisitionMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("reveals their hand"));
    }

    @Test
    @DisplayName("Does not count nonwhite cards in the target player's hand")
    void ignoresNonwhiteCardsInHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Inquisition()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addInquisitionMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Inquisition(), new SavannahLions()));
        addInquisitionMana();

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void rejectsPermanentTarget() {
        harness.setHand(player1, List.of(new Inquisition()));
        addInquisitionMana();
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                gd.playerBattlefields.get(player2.getId()).getFirst().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addInquisitionMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
