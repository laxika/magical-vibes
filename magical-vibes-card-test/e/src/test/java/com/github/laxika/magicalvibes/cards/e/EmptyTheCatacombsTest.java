package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmptyTheCatacombs.class, BorosRecruit.class, BorosSignet.class})
class EmptyTheCatacombsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all creature cards from each player's graveyard to their hand")
    void returnsAllCreatureCardsFromEachPlayersGraveyardToTheirHand() {
        Card player1Creature = new BorosRecruit();
        Card player1SecondCreature = new BorosRecruit();
        Card player1NonCreature = new BorosSignet();
        Card player2Creature = new BorosRecruit();
        Card player2SecondCreature = new BorosRecruit();
        Card player2NonCreature = new BorosSignet();

        harness.setGraveyard(player1, List.of(player1Creature, player1SecondCreature, player1NonCreature));
        harness.setGraveyard(player2, List.of(player2Creature, player2SecondCreature, player2NonCreature));
        harness.castFromHand(player1, new EmptyTheCatacombs(), "{3}{B}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(player1Creature.getId())
                .contains(player1SecondCreature.getId())
                .doesNotContain(player2Creature.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .contains(player2Creature.getId())
                .contains(player2SecondCreature.getId())
                .doesNotContain(player1Creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(player1NonCreature.getId())
                .doesNotContain(player1Creature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(player2NonCreature.getId())
                .doesNotContain(player2Creature.getId());
    }
    @Test
    @DisplayName("Returns the opponent's creatures even when the caster's graveyard is empty")
    void returnsOpponentsCreaturesWithEmptyControllersGraveyard() {
        Card creature = new BorosRecruit();
        Card artifact = new BorosSignet();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(creature, artifact));
        harness.setHand(player2, List.of());

        harness.castFromHand(player1, new EmptyTheCatacombs(), "{3}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(artifact);
        harness.assertInGraveyard(player1, "Empty the Catacombs");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves when neither graveyard contains creatures")
    void resolvesWithoutCreatureCards() {
        Card artifact = new BorosSignet();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of());
        Card spell = new EmptyTheCatacombs();

        harness.castFromHand(player1, spell, "{3}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact, spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns creatures present at resolution rather than those present when cast")
    void usesGraveyardsAtResolution() {
        Card removedCreature = new BorosRecruit();
        Card addedCreature = new BorosRecruit();
        Card artifact = new BorosSignet();
        harness.setGraveyard(player1, List.of(removedCreature));
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new EmptyTheCatacombs(), "{3}{B}");

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(removedCreature));
        harness.setGraveyard(player2, List.of(artifact, addedCreature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(addedCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(artifact);
        assertThat(gd.findExiledCard(removedCreature.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }
}
