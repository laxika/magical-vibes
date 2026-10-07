package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WithGreatPower;
import com.github.laxika.magicalvibes.cards.w.WebShooters;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunSpiderNimbleWebber.class, WithGreatPower.class, WebShooters.class})
class SunSpiderNimbleWebberTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying during its controller's turn only")
    void flyingOnlyDuringControllerTurn() {
        Permanent sunSpider = harness.addToBattlefieldAndReturn(player1, new SunSpiderNimbleWebber());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, sunSpider, Keyword.FLYING)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, sunSpider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Enters by searching for an Aura or Equipment card")
    void entersSearchesForAuraOrEquipment() {
        harness.setLibrary(player1, List.of(new WithGreatPower(), new WebShooters()));
        harness.setHand(player1, List.of(new SunSpiderNimbleWebber()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards())
                .anyMatch(card -> card.getSubtypes().contains(CardSubtype.AURA));
        assertThat(search.params().cards())
                .anyMatch(card -> card.getSubtypes().contains(CardSubtype.EQUIPMENT));

        int auraIndex = 0;
        for (int i = 0; i < search.params().cards().size(); i++) {
            Card card = search.params().cards().get(i);
            if (card.getSubtypes().contains(CardSubtype.AURA)) {
                auraIndex = i;
                break;
            }
        }
        harness.handleCardChosen(player1, auraIndex);

        harness.assertInHand(player1, "With Great Power . . .");
    }

    @Test
    @DisplayName("Can select Equipment, revealing it and leaving other cards in the library")
    void searchesForEquipmentAndRevealsIt() {
        WebShooters equipment = new WebShooters();
        SunSpiderNimbleWebber ineligible = new SunSpiderNimbleWebber();
        harness.setLibrary(player1, List.of(ineligible, equipment));
        harness.setLibrary(player2, List.of(new WithGreatPower()));
        castAndResolveEntryTrigger(ManaColor.BLUE);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(equipment);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Web-Shooters");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ineligible);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains(
                "reveals Web-Shooters and puts it into their hand."));
    }

    @Test
    @DisplayName("May fail to find even when an eligible card is present")
    void canFailToFind() {
        WebShooters equipment = new WebShooters();
        harness.setLibrary(player1, List.of(equipment));
        castAndResolveEntryTrigger(ManaColor.WHITE);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Resolves without finding a card when the library has no Aura or Equipment")
    void noEligibleCards() {
        SunSpiderNimbleWebber ineligible = new SunSpiderNimbleWebber();
        harness.setLibrary(player1, List.of(ineligible));
        castAndResolveEntryTrigger(ManaColor.WHITE);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ineligible);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Resolves the search with an empty library")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        castAndResolveEntryTrigger(ManaColor.WHITE);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Flying follows each permanent's controller and returns on the next own turn")
    void flyingIsRestrictedToEachController() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SunSpiderNimbleWebber());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SunSpiderNimbleWebber());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isFalse();
        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isFalse();
    }

    private void castAndResolveEntryTrigger(ManaColor hybridColor) {
        harness.setHand(player1, List.of(new SunSpiderNimbleWebber()));
        harness.addMana(player1, hybridColor, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
