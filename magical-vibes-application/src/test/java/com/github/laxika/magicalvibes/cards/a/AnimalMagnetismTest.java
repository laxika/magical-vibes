package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnimalMagnetism.class, ElvishWarrior.class, Shock.class, Forest.class})
class AnimalMagnetismTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses a revealed creature, which enters under the controller's control")
    void opponentChoosesCreatureForBattlefield() {
        Card firstCreature = new ElvishWarrior();
        Card nonCreatureOne = new Shock();
        Card secondCreature = new ElvishWarrior();
        Card nonCreatureTwo = new Forest();
        Card nonCreatureThree = new Shock();
        Card untouched = new Shock();
        Card spell = new AnimalMagnetism();
        harness.setLibrary(player1, List.of(
                firstCreature, nonCreatureOne, secondCreature, nonCreatureTwo, nonCreatureThree, untouched));
        harness.castFromHand(player1, spell, "{4}{G}");
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(firstCreature.getId(), secondCreature.getId());
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player2, List.of(secondCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .containsExactly(secondCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(spell, firstCreature, nonCreatureOne,
                        nonCreatureTwo, nonCreatureThree);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("If no creature is revealed, all revealed cards go to the graveyard")
    void noCreatureIsRevealed() {
        Card first = new Shock();
        Card second = new Forest();
        Card third = new Shock();
        Card fourth = new Forest();
        Card fifth = new Shock();
        Card spell = new AnimalMagnetism();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.castFromHand(player1, spell, "{4}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(spell, first, second, third, fourth, fifth);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A short library reveals and processes all remaining cards")
    void shortLibraryRevealsAllRemainingCards() {
        Card creature = new ElvishWarrior();
        Card nonCreature = new Shock();
        Card anotherNonCreature = new Forest();
        AnimalMagnetism spell = new AnimalMagnetism();
        harness.setLibrary(player1, List.of(creature, nonCreature, anotherNonCreature));
        harness.castFromHand(player1, spell, "{4}{G}");
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player2, List.of(creature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(spell, nonCreature, anotherNonCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without opening a choice")
    void emptyLibraryResolvesWithoutChoice() {
        AnimalMagnetism spell = new AnimalMagnetism();
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, spell, "{4}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
