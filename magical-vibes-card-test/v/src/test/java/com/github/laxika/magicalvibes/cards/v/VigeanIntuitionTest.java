package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AzoriusChancery;
import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.b.BronzeBombshell;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VigeanIntuition.class, AzoriusChancery.class, AzoriusFirstWing.class, VisionSkeins.class, BronzeBombshell.class})
class VigeanIntuitionTest extends BaseCardTest {

    @Test
    void putsCardsOfChosenTypeIntoHandAndTheRestIntoGraveyard() {
        Card firstLand = new AzoriusChancery();
        Card secondLand = new AzoriusChancery();
        Card creature = new AzoriusFirstWing();
        Card instant = new VisionSkeins();
        harness.setLibrary(player1, List.of(firstLand, secondLand, creature, instant));

        harness.castFromHand(player1, new VigeanIntuition(), "{3}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, CardType.LAND.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstLand, secondLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature, instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void processesTheAvailableCardsWhenLibraryHasFewerThanFour() {
        Card land = new AzoriusChancery();
        Card creature = new AzoriusFirstWing();
        Card instant = new VisionSkeins();
        harness.setLibrary(player1, List.of(land, creature, instant));

        harness.castFromHand(player1, new VigeanIntuition(), "{3}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, CardType.CREATURE.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void choosesTypeBeforeRevealingAndLeavesFifthCardInLibrary() {
        Card land = new AzoriusChancery();
        Card creature = new AzoriusFirstWing();
        Card instant = new VisionSkeins();
        Card secondInstant = new VisionSkeins();
        Card fifth = new AzoriusChancery();
        harness.setLibrary(player1, List.of(land, creature, instant, secondInstant, fifth));

        harness.castFromHand(player1, new VigeanIntuition(), "{3}{G}{U}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        assertThat(choice.options()).contains(CardType.INSTANT.name(), CardType.KINDRED.name(),
                CardType.BATTLE.name(), CardType.PLANESWALKER.name())
                .doesNotContain(CardType.EMBLEM.name());
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(land, creature, instant, secondInstant, fifth);
        harness.handleListChoice(player1, CardType.INSTANT.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant, secondInstant);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth);
    }

    @Test
    void putsAllRevealedCardsIntoGraveyardWhenNoneMatch() {
        Card land = new AzoriusChancery();
        Card creature = new AzoriusFirstWing();
        Card instant = new VisionSkeins();
        harness.setLibrary(player1, List.of(land, creature, instant));

        harness.castFromHand(player1, new VigeanIntuition(), "{3}{G}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardType.ARTIFACT.name());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, creature, instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void putsEveryMatchingCardIntoHand() {
        Card first = new AzoriusFirstWing();
        Card second = new AzoriusFirstWing();
        Card third = new AzoriusFirstWing();
        Card fourth = new AzoriusFirstWing();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        harness.castFromHand(player1, new VigeanIntuition(), "{3}{G}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardType.CREATURE.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third, fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second, third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void resolvesWithEmptyLibraryWithoutDrawing() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new VigeanIntuition(), "{3}{G}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardType.LAND.name());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Vigean Intuition");
    }

    @Test
    void matchesCreatureTypeOnAnArtifactCreature() {
        Card artifactCreature = new BronzeBombshell();
        Card land = new AzoriusChancery();
        harness.setLibrary(player1, List.of(artifactCreature, land));

        harness.castFromHand(player1, new VigeanIntuition(), "{3}{G}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardType.CREATURE.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifactCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land).doesNotContain(artifactCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void matchesArtifactTypeOnAnArtifactCreature() {
        Card artifactCreature = new BronzeBombshell();
        Card creature = new AzoriusFirstWing();
        harness.setLibrary(player1, List.of(artifactCreature, creature));

        harness.castFromHand(player1, new VigeanIntuition(), "{3}{G}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardType.ARTIFACT.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifactCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature).doesNotContain(artifactCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
