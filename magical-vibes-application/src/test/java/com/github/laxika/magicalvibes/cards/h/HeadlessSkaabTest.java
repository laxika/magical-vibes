package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeadlessSkaabTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Can cast by exiling a creature card from graveyard")
    void castExilesCreatureFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

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
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));

        harness.setHand(player1, List.of(new HeadlessSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Index 1 is a Shock (instant), not a creature
        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

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
