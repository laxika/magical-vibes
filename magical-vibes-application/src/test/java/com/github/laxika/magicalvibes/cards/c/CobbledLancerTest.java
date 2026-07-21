package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CobbledLancerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Cobbled Lancer exiles a creature card from graveyard")
    void castingExilesCreatureFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CobbledLancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Cobbled Lancer");

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot cast without a creature in graveyard")
    void cannotCastWithoutCreatureInGraveyard() {
        harness.setHand(player1, List.of(new CobbledLancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a non-creature card from graveyard")
    void cannotExileNonCreatureCard() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new CobbledLancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Resolves onto the battlefield after paying exile cost")
    void entersBattlefieldAfterExileCost() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CobbledLancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cobbled Lancer");
    }

    @Test
    @DisplayName("Graveyard ability exiles the source and draws a card")
    void graveyardAbilityExilesAndDraws() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new CobbledLancer()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Cobbled Lancer"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cobbled Lancer"));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
