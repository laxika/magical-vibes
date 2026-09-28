package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JunkDiver;
import com.github.laxika.magicalvibes.cards.o.Opportunity;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VictorTimelyWilyTycoon.class, BraidwoodCup.class, Divination.class, Forest.class,
        GrizzlyBears.class, JunkDiver.class, Opportunity.class, Shock.class})
class VictorTimelyWilyTycoonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets nonland artifacts, instants, and sorceries with mana value 4 or less")
    void etbTargetsMatchingCards() {
        Card artifact = new BraidwoodCup();
        Card artifactCreature = new JunkDiver();
        Card instant = new Shock();
        Card sorcery = new Divination();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card expensive = new Opportunity();
        harness.setGraveyard(player1, List.of(artifact, artifactCreature, instant, sorcery,
                creature, land, expensive));

        castVictor();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                artifact.getId(), artifactCreature.getId(), instant.getId(), sorcery.getId());
    }

    @Test
    @DisplayName("ETB casts the chosen card for free and exiles it afterward")
    void castsChosenSorceryForFreeAndExilesIt() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        castVictor();
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(divination.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(divination.getId()));
    }

    @Test
    @DisplayName("ETB does not trigger when the graveyard has no matching card")
    void noMatchingCardDoesNotPrompt() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card expensive = new Opportunity();
        harness.setGraveyard(player1, List.of(creature, land, expensive));

        castVictor();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(creature.getId(), land.getId(), expensive.getId());
    }

    private void castVictor() {
        harness.setHand(player1, List.of(new VictorTimelyWilyTycoon()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
