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
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A drawn opponent spell can be cast by paying exactly the extra generic mana")
    void opponentPaysExactlyOneExtraMana() {
        harness.addToBattlefield(player1, new CaptainEberhart());
        drawCard(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent cards that were not drawn this turn retain their normal cost")
    void doesNotIncreaseUndrawnOpponentCards() {
        harness.addToBattlefield(player1, new CaptainEberhart());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The discount cannot pay the spell's colored mana requirement")
    void discountDoesNotReduceColoredMana() {
        harness.addToBattlefield(player1, new CaptainEberhart());
        drawCard(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Captains controlled by opposing players have cancelling cost modifiers")
    void opposingCaptainsCancelDiscountAndTax() {
        harness.addToBattlefield(player1, new CaptainEberhart());
        harness.addToBattlefield(player2, new CaptainEberhart());
        drawCard(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Double strike deals damage in both combat damage steps")
    void dealsCombatDamageTwice() {
        addCreatureReady(player1, new CaptainEberhart());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    private void drawCard(Player player, GrizzlyBears card) {
        harness.setLibrary(player, List.of(card));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.setHand(player, List.of(card));
    }
}
