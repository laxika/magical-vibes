package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SylvanReclamation.class, AuraOfSilence.class, Forest.class, GrizzlyBears.class, Ornithopter.class})
class SylvanReclamationTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to two target artifacts and enchantments")
    void exilesTwoTargets() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new Ornithopter()).getId();
        UUID enchantmentId = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence()).getId();

        harness.setHand(player1, List.of(new SylvanReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, List.of(artifactId, enchantmentId));

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Aura of Silence");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Ornithopter", "Aura of Silence");
    }

    @Test
    @DisplayName("Cannot target a nonartifact, nonenchantment permanent")
    void cannotTargetCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new SylvanReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Basic landcycling searches for a basic land")
    void basicLandcyclingSearchesForBasicLand() {
        SylvanReclamation reclamation = new SylvanReclamation();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(reclamation));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Sylvan Reclamation");
    }

    @Test
    void canResolveWithoutTargets() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new SylvanReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, List.<UUID>of());

        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Sylvan Reclamation");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void canExileOneOfItsControllersArtifacts() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Ornithopter()).getId();
        harness.setHand(player1, List.of(new SylvanReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, List.of(targetId));

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName).containsExactly("Ornithopter");
    }

    @Test
    void cannotChooseMoreThanTwoTargets() {
        UUID first = harness.addToBattlefieldAndReturn(player2, new Ornithopter()).getId();
        UUID second = harness.addToBattlefieldAndReturn(player2, new Ornithopter()).getId();
        UUID third = harness.addToBattlefieldAndReturn(player2, new Ornithopter()).getId();
        harness.setHand(player1, List.of(new SylvanReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first, second, third)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseTheSameTargetTwice() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Ornithopter()).getId();
        harness.setHand(player1, List.of(new SylvanReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(targetId, targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void basicLandcyclingCanFailToFindAndDoesNotDraw() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new SylvanReclamation()));
        harness.setLibrary(player1, List.of(forest, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Sylvan Reclamation");
        harness.assertNotInHand(player1, "Sylvan Reclamation");
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactlyInAnyOrder("Forest", "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void basicLandcyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new SylvanReclamation()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Sylvan Reclamation");
        harness.assertNotInGraveyard(player1, "Sylvan Reclamation");
        assertThat(gd.stack).isEmpty();
    }
}
