package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SuddenReclamationTest extends BaseCardTest {

    @Test
    @DisplayName("Mills four cards, then returns a creature and a land from the graveyard")
    void millsFourThenReturnsCreatureAndLand() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card milledOne = new Shock();
        Card milledTwo = new Shock();
        Card milledThree = new Shock();
        Card milledFour = new Shock();
        Card spell = new SuddenReclamation();

        harness.setGraveyard(player1, List.of(creature, land));
        harness.setLibrary(player1, List.of(milledOne, milledTwo, milledThree, milledFour));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature, land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(milledOne, milledTwo, milledThree, milledFour,
                        spell);
    }

    @Test
    @DisplayName("Still returns the creature when no land is available")
    void returnsCreatureWithoutLand() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new SuddenReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Returns one chosen creature and one chosen land, with separate resolution-time choices")
    void choosesCreatureAndLandSeparately() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature, firstLand, secondLand));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new SuddenReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(creatureChoice).isNotNull();
        assertThat(creatureChoice.validIndices())
                .containsExactly(gd.playerGraveyards.get(player1.getId()).indexOf(firstCreature),
                        gd.playerGraveyards.get(player1.getId()).indexOf(secondCreature));

        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(secondCreature));

        PendingInteraction.GraveyardChoice landChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(landChoice).isNotNull();
        assertThat(landChoice.validIndices())
                .containsExactly(gd.playerGraveyards.get(player1.getId()).indexOf(firstLand),
                        gd.playerGraveyards.get(player1.getId()).indexOf(secondLand));

        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(secondLand));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondCreature, secondLand);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstCreature, firstLand);
    }
}
