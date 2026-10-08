package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SojournersCompanion.class, Spellbook.class, SilverbluffBridge.class, Forest.class, GrizzlyBears.class})
class SojournersCompanionTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new SojournersCompanion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Artifact landcycling searches only for artifact lands")
    void artifactLandcyclingSearchesForArtifactLand() {
        SilverbluffBridge bridge = new SilverbluffBridge();
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new SojournersCompanion()));
        harness.setLibrary(player1, List.of(bridge, forest, bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(bridge);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Sojourner's Companion");
        harness.assertInHand(player1, "Silverbluff Bridge");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, bears);
    }

    @Test
    @DisplayName("Affinity can reduce the casting cost to zero")
    void affinityCanReduceCastingCostToZero() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new SilverbluffBridge());
        }
        harness.setHand(player1, List.of(new SojournersCompanion()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sojourner's Companion");
    }

    @Test
    @DisplayName("Affinity counts only artifacts on the caster's battlefield")
    void affinityIgnoresOpponentsArtifactsAndCardsInOtherZones() {
        harness.addToBattlefield(player1, new SilverbluffBridge());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new SilverbluffBridge());
        harness.setHand(player1, List.of(new SojournersCompanion(), new SojournersCompanion()));
        harness.setGraveyard(player1, List.of(new SojournersCompanion()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Artifact landcycling pays mana and discards before resolving without drawing")
    void artifactLandcyclingPaysCostsBeforeResolutionWithoutDrawing() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new SilverbluffBridge());
        }
        SilverbluffBridge bridge = new SilverbluffBridge();
        harness.setHand(player1, List.of(new SojournersCompanion()));
        harness.setLibrary(player1, List.of(bridge));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Sojourner's Companion");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bridge);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bridge);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("reveals Silverbluff Bridge")).isTrue();
    }

    @Test
    @DisplayName("Affinity does not reduce artifact landcycling's activation cost")
    void affinityDoesNotReduceArtifactLandcyclingCost() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new SilverbluffBridge());
        }
        harness.setHand(player1, List.of(new SojournersCompanion()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Sojourner's Companion");
        harness.assertNotInGraveyard(player1, "Sojourner's Companion");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Artifact landcycling may fail to find even with an artifact land available")
    void artifactLandcyclingMayFailToFind() {
        SilverbluffBridge bridge = new SilverbluffBridge();
        harness.setHand(player1, List.of(new SojournersCompanion()));
        harness.setLibrary(player1, List.of(bridge));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Sojourner's Companion");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bridge);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Artifact landcycling resolves when the library contains no artifact lands")
    void artifactLandcyclingWithNoMatchingCards() {
        SojournersCompanion artifactCreature = new SojournersCompanion();
        Forest land = new Forest();
        harness.setHand(player1, List.of(new SojournersCompanion()));
        harness.setLibrary(player1, List.of(artifactCreature, land));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sojourner's Companion");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(artifactCreature, land);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Artifact landcycling resolves with an empty library")
    void artifactLandcyclingWithEmptyLibrary() {
        harness.setHand(player1, List.of(new SojournersCompanion()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sojourner's Companion");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
