package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MuddleTheMixture.class, Counterspell.class, Divination.class, GrizzlyBears.class, Shock.class})
class MuddleTheMixtureTest extends BaseCardTest {

    @Test
    void countersInstantSpell() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new MuddleTheMixture()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.setLife(player2, 20);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertLife(player2, 20);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void countersSorcerySpell() {
        Divination divination = new Divination();
        Shock firstLibraryCard = new Shock();
        Shock secondLibraryCard = new Shock();
        harness.setHand(player1, List.of(divination));
        harness.setLibrary(player1, List.of(firstLibraryCard, secondLibraryCard));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new MuddleTheMixture()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, divination.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Divination");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .containsExactly(firstLibraryCard, secondLibraryCard);
    }

    @Test
    void cannotTargetCreatureSpell() {
        GrizzlyBears grizzlyBears = new GrizzlyBears();
        harness.setHand(player1, List.of(grizzlyBears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new MuddleTheMixture()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, grizzlyBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transmuteSearchesForTheSameManaValue() {
        MuddleTheMixture muddle = new MuddleTheMixture();
        Counterspell matchingCard = new Counterspell();
        Shock differentManaValue = new Shock();
        harness.setHand(player1, List.of(muddle));
        harness.setLibrary(player1, List.of(matchingCard, differentManaValue));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Muddle the Mixture");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }
}
