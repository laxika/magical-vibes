package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AinokGuide;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.Refocus;
import com.github.laxika.magicalvibes.cards.t.TormodTheDesecrator;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuddenReclamation.class, Forest.class, GrizzlyBears.class, Shock.class,
        AinokGuide.class, Refocus.class, TormodTheDesecrator.class})
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

        harness.castAndResolveInstant(player1, 0);

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

        harness.castAndResolveInstant(player1, 0);

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

        harness.castAndResolveInstant(player1, 0);

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

    @Test
    void returnsFreshlyMilledCardsFromAShortLibrary() {
        Card creature = new AinokGuide();
        Card land = new Forest();
        Card spell = new SuddenReclamation();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(creature, land));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void returnsLandWithoutCreatureEvenWithAnEmptyLibrary() {
        Card land = new Forest();
        Card opponentCreature = new AinokGuide();
        harness.setGraveyard(player1, List.of(land));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new SuddenReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWhenNeitherCardTypeIsAvailable() {
        Card milled = new Refocus();
        Card spell = new SuddenReclamation();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(milled));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(milled, spell);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void returningCreatureAndLandTogetherTriggersTormodOnlyOnce() {
        harness.addToBattlefield(player1, new TormodTheDesecrator());
        Card creature = new AinokGuide();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new SuddenReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }
}
