package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EncroachingMycosynth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MishraArtificerProdigy.class, Spellbook.class, GrizzlyBears.class, EncroachingMycosynth.class})
class MishraArtificerProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact offers the same-name battlefield search")
    void artifactCastOffersSameNameSearch() {
        harness.addToBattlefield(player1, new MishraArtificerProdigy());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellbook");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the search leaves the matching card in hand")
    void decliningSearchLeavesCardInHand() {
        harness.addToBattlefield(player1, new MishraArtificerProdigy());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Spellbook");
        harness.assertNotOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Casting a nonartifact spell does not trigger the search")
    void nonartifactCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new MishraArtificerProdigy());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("The search can put a matching graveyard card onto the battlefield")
    void findsMatchingCardInGraveyard() {
        harness.addToBattlefield(player1, new MishraArtificerProdigy());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.setGraveyard(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertNotInGraveyard(player1, "Spellbook");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The search can put a matching library card onto the battlefield")
    void findsMatchingCardInLibrary() {
        harness.addToBattlefield(player1, new MishraArtificerProdigy());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.setLibrary(player1, List.of(new Spellbook(), new GrizzlyBears()));

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Spellbook");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A library search may fail to find even when a matching card exists")
    void mayFailToFindInLibrary() {
        harness.addToBattlefield(player1, new MishraArtificerProdigy());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.setLibrary(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Spellbook");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A graveyard match does not force its selection over hand and library matches")
    void matchingGraveyardCardDoesNotPreventChoosingOtherZones() {
        harness.addToBattlefield(player1, new MishraArtificerProdigy());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));
        harness.setGraveyard(player1, List.of(new Spellbook()));
        harness.setLibrary(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertInHand(player1, "Spellbook");
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    @DisplayName("An opponent casting an artifact does not trigger Mishra")
    void opponentsArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new MishraArtificerProdigy());
        harness.setHand(player2, List.of(new Spellbook()));
        harness.forceActivePlayer(player2);

        harness.castArtifact(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A creature spell made an artifact by Encroaching Mycosynth triggers Mishra")
    void grantedArtifactTypeTriggersSearch() {
        harness.addToBattlefield(player1, new MishraArtificerProdigy());
        harness.addToBattlefield(player1, new EncroachingMycosynth());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }
}
