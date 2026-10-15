package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JaliraMasterPolymorphist.class, RuneclawBear.class, DarksteelCitadel.class})
class JaliraMasterPolymorphistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices another creature and puts the first nonlegendary creature revealed onto the battlefield")
    void polymorphsAnotherCreature() {
        addCreatureReady(player1, new JaliraMasterPolymorphist());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setLibrary(player1, List.of(
                new JaliraMasterPolymorphist(),
                new DarksteelCitadel(),
                new RuneclawBear()));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        assertThat(deck).extracting(Card::getName)
                .containsExactlyInAnyOrder("Jalira, Master Polymorphist", "Darksteel Citadel");
    }

    @Test
    @DisplayName("Puts all revealed cards on the bottom when no nonlegendary creature is found")
    void noMatchingCreatureReturnsRevealedCardsToLibrary() {
        addCreatureReady(player1, new JaliraMasterPolymorphist());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setLibrary(player1, List.of(new JaliraMasterPolymorphist(), new DarksteelCitadel()));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(deck).extracting(Card::getName)
                .containsExactlyInAnyOrder("Jalira, Master Polymorphist", "Darksteel Citadel");
    }

    @Test
    void stopsAtFirstMatchAndPreservesUnrevealedCardsAboveBottomedCards() {
        var jalira = addCreatureReady(player1, new JaliraMasterPolymorphist());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card skippedLegend = new JaliraMasterPolymorphist();
        Card skippedArtifact = new DarksteelCitadel();
        Card firstMatch = new RuneclawBear();
        Card unrevealedCreature = new RuneclawBear();
        Card unrevealedArtifact = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(skippedLegend, skippedArtifact, firstMatch,
                unrevealedCreature, unrevealedArtifact));

        harness.activateAbility(player1, 0, null, null);

        assertThat(jalira.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(p -> {
                    assertThat(p.getCard()).isSameAs(firstMatch);
                    assertThat(p.isTapped()).isFalse();
                    assertThat(p.isSummoningSick()).isTrue();
                });
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.subList(0, 2)).containsExactly(unrevealedCreature, unrevealedArtifact);
        assertThat(deck.subList(2, 4)).containsExactlyInAnyOrder(skippedLegend, skippedArtifact);
    }

    @Test
    void emptyLibraryStillPaysSacrificeCost() {
        addCreatureReady(player1, new JaliraMasterPolymorphist());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotSacrificeItselfOrOpponentsCreature() {
        addCreatureReady(player1, new JaliraMasterPolymorphist());
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Jalira, Master Polymorphist");
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    void summoningSickJaliraCannotActivate() {
        harness.addToBattlefield(player1, new JaliraMasterPolymorphist());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    void cannotActivateWithoutBlueMana() {
        addCreatureReady(player1, new JaliraMasterPolymorphist());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }
}
