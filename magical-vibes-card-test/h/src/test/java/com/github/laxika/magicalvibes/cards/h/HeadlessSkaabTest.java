package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.ScreechingSkaab;
import com.github.laxika.magicalvibes.cards.t.ThoughtScour;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeadlessSkaab.class, ScreechingSkaab.class, ThoughtScour.class})
class HeadlessSkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast by exiling a creature card from graveyard")
    void castExilesCreatureFromGraveyard() {
        harness.setGraveyard(player1, List.of(new ScreechingSkaab()));

        harness.setHand(player1, List.of(new HeadlessSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Headless Skaab");

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot cast without a creature card in graveyard")
    void cannotCastWithoutCreatureInGraveyard() {
        harness.setHand(player1, List.of(new HeadlessSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a non-creature card to pay the additional cost")
    void cannotExileNonCreatureCard() {
        harness.setGraveyard(player1, List.of(new ScreechingSkaab(), new ThoughtScour()));

        harness.setHand(player1, List.of(new HeadlessSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Index 1 is Thought Scour (an instant), not a creature
        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void exilesOnlyTheSelectedCreatureAsCastingCost() {
        var unselected = new ScreechingSkaab();
        var selected = new ScreechingSkaab();
        var instant = new ThoughtScour();
        harness.setGraveyard(player1, List.of(unselected, instant, selected));
        harness.setHand(player1, List.of(new HeadlessSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(2));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselected, instant);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(selected);
        harness.assertNotOnBattlefield(player1, "Headless Skaab");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Headless Skaab");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(selected);
    }

    @Test
    void cannotExileMoreThanOneCreatureForTheCost() {
        harness.setGraveyard(player1, List.of(new ScreechingSkaab(), new ScreechingSkaab()));
        harness.setHand(player1, List.of(new HeadlessSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Headless Skaab");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayWithAnOpponentsCreatureCard() {
        var opponentCreature = new ScreechingSkaab();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new HeadlessSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
        harness.assertInHand(player1, "Headless Skaab");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersTappedWithoutBeingCastAndRequiresNoExilePayment() {
        harness.setGraveyard(player1, List.of());

        var permanent = harness.enterBattlefieldAndReturn(player1, new HeadlessSkaab());

        harness.assertOnBattlefield(player1, "Headless Skaab");
        assertThat(permanent.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setGraveyard(player1, List.of(new ScreechingSkaab()));

        harness.setHand(player1, List.of(new HeadlessSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Headless Skaab");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Headless Skaab"))
                .singleElement()
                .satisfies(p -> assertThat(p.isTapped()).isTrue());
    }
}
