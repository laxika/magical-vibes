package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptainEberhart.class, GrizzlyBears.class})
class CaptainEberhartTest extends BaseCardTest {

    @Test
    @DisplayName("Spells cast from cards drawn this turn cost {1} less")
    void reducesSpellsCastFromOwnDrawnCards() {
        harness.addToBattlefield(player1, new CaptainEberhart());
        GrizzlyBears bears = new GrizzlyBears();
        drawCard(player1, bears);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Spells cast from cards not drawn this turn are not reduced")
    void doesNotReduceUndrawnCards() {
        harness.addToBattlefield(player1, new CaptainEberhart());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spells cast from cards opponents drew this turn cost {1} more")
    void increasesSpellsCastFromOpponentsDrawnCards() {
        harness.addToBattlefield(player1, new CaptainEberhart());
        GrizzlyBears bears = new GrizzlyBears();
        drawCard(player2, bears);
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void drawCard(Player player, GrizzlyBears card) {
        harness.setLibrary(player, List.of(card));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.setHand(player, List.of(card));
    }
}
