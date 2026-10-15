package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MuzzioVisionaryArchitect.class, WornPowerstone.class, GildedLotus.class,
        Ornithopter.class, GrizzlyBears.class})
class MuzzioVisionaryArchitectTest extends BaseCardTest {

    @Test
    @DisplayName("Uses the greatest mana value among your artifacts and offers only artifacts in range")
    void usesControlledArtifactManaValueAndArtifactFilter() {
        addCreatureReady(player1, new MuzzioVisionaryArchitect());
        harness.addToBattlefield(player1, new WornPowerstone());
        harness.addToBattlefield(player2, new GildedLotus());

        Card nonArtifact = new GrizzlyBears();
        Card firstArtifact = new Ornithopter();
        Card secondArtifact = new GildedLotus();
        Card unlookedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonArtifact, firstArtifact, secondArtifact, unlookedCard));
        activate();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards();
        assertThat(offered).extracting(Card::getName)
                .containsExactly("Ornithopter", "Gilded Lotus");

        harness.handleCardChosen(player1, 1);
        harness.assertOnBattlefield(player1, "Gilded Lotus");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(unlookedCard, firstArtifact, nonArtifact);
    }

    @Test
    @DisplayName("Declining leaves all looked-at cards on the bottom")
    void mayDeclineArtifactChoice() {
        addCreatureReady(player1, new MuzzioVisionaryArchitect());
        harness.addToBattlefield(player1, new WornPowerstone());

        Card firstArtifact = new Ornithopter();
        Card nonArtifact = new GrizzlyBears();
        Card secondArtifact = new GildedLotus();
        Card unlookedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstArtifact, nonArtifact, secondArtifact, unlookedCard));
        activate();

        harness.handleCardChosen(player1, -1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(unlookedCard, secondArtifact, nonArtifact, firstArtifact);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(firstArtifact.getId()));
    }

    @Test
    @DisplayName("An opponent's artifacts do not increase the number of cards looked at")
    void opponentArtifactsDoNotCount() {
        Permanent setup = setupMuzzioWithoutControlledArtifact();
        harness.addToBattlefield(player2, new GildedLotus());
        Card topCard = new Ornithopter();
        Card nextCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        activate();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(setup.isTapped()).isTrue();
    }

    private void activate() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    void zeroManaValueArtifactDoesNotLookAtAnyCards() {
        addCreatureReady(player1, new MuzzioVisionaryArchitect());
        harness.addToBattlefield(player1, new Ornithopter());
        Card topCard = new GildedLotus();
        harness.setLibrary(player1, List.of(topCard));

        activate();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertNotOnBattlefield(player1, "Gilded Lotus");
    }

    @Test
    void noMatchingArtifactsStillAllowsBottomOrdering() {
        addCreatureReady(player1, new MuzzioVisionaryArchitect());
        harness.addToBattlefield(player1, new WornPowerstone());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card unlooked = new Ornithopter();
        harness.setLibrary(player1, List.of(first, second, third, unlooked));

        activate();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unlooked, third, first, second);
    }

    @Test
    void shortLibraryAllowsArtifactToEnterWithItsOwnTappedReplacement() {
        addCreatureReady(player1, new MuzzioVisionaryArchitect());
        harness.addToBattlefield(player1, new GildedLotus());
        Card selected = new WornPowerstone();
        Card remaining = new GrizzlyBears();
        harness.setLibrary(player1, List.of(selected, remaining));

        activate();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(selected.getId());
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void artifactManaValueIsEvaluatedWhenAbilityResolves() {
        addCreatureReady(player1, new MuzzioVisionaryArchitect());
        harness.addToBattlefield(player1, new WornPowerstone());
        Card topCard = new Ornithopter();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void emptyLibraryDoesNotRequestAChoice() {
        addCreatureReady(player1, new MuzzioVisionaryArchitect());
        harness.addToBattlefield(player1, new WornPowerstone());
        harness.setLibrary(player1, List.of());

        activate();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void usesGreatestManaValueRatherThanSumOrNumberOfArtifacts() {
        addCreatureReady(player1, new MuzzioVisionaryArchitect());
        harness.addToBattlefield(player1, new WornPowerstone());
        harness.addToBattlefield(player1, new GildedLotus());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        Card fifth = new Ornithopter();
        Card unlooked = new GildedLotus();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, unlooked));

        activate();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(fifth);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Ornithopter");
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unlooked, fourth, third, second, first);
    }

    private Permanent setupMuzzioWithoutControlledArtifact() {
        return addCreatureReady(player1, new MuzzioVisionaryArchitect());
    }
}
