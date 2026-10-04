package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SentinelTotem;
import com.github.laxika.magicalvibes.cards.j.JacesSentinel;
import com.github.laxika.magicalvibes.cards.j.JaceIngeniousMindMage;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraspingCurrent.class, JacesSentinel.class, SentinelTotem.class, JaceIngeniousMindMage.class})
class GraspingCurrentTest extends BaseCardTest {

    @Test
    @DisplayName("Bounces two target creatures to their owners' hands")
    void bouncesTwoCreatures() {
        harness.addToBattlefield(player2, new JacesSentinel());
        harness.addToBattlefield(player2, new JacesSentinel());
        List<UUID> targetIds = gd.playerBattlefields.get(player2.getId()).stream()
                .map(p -> p.getId()).toList();
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, targetIds);

        harness.assertNotOnBattlefield(player2, "Jace's Sentinel");
        assertThat(gd.playerHands.get(player2.getId()).stream()
                .filter(c -> c.getName().equals("Jace's Sentinel")).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target only one creature")
    void bouncesOneCreature() {
        harness.addToBattlefield(player2, new JacesSentinel());
        UUID targetId = harness.getPermanentId(player2, "Jace's Sentinel");
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        harness.assertNotOnBattlefield(player2, "Jace's Sentinel");
        harness.assertInHand(player2, "Jace's Sentinel");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new SentinelTotem());
        UUID artifactId = harness.getPermanentId(player2, "Sentinel Totem");
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(artifactId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bounces creatures and finds Jace in graveyard")
    void bouncesAndFindsJaceInGraveyard() {
        Card jace = new JaceIngeniousMindMage();

        harness.addToBattlefield(player2, new JacesSentinel());
        UUID targetId = harness.getPermanentId(player2, "Jace's Sentinel");
        harness.setGraveyard(player1, List.of(jace));
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        // Creature bounced
        harness.assertNotOnBattlefield(player2, "Jace's Sentinel");
        harness.assertInHand(player2, "Jace's Sentinel");
        // Jace moved from graveyard to hand
        harness.assertInHand(player1, "Jace, Ingenious Mind-Mage");
        harness.assertNotInGraveyard(player1, "Jace, Ingenious Mind-Mage");
    }

    @Test
    @DisplayName("Does not find Jace when not in graveyard or library")
    void doesNotFindJace() {
        harness.addToBattlefield(player2, new JacesSentinel());
        UUID targetId = harness.getPermanentId(player2, "Jace's Sentinel");
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        // Creature still bounced
        harness.assertNotOnBattlefield(player2, "Jace's Sentinel");
        // No Jace found
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void searchesLibraryWithNoCreatureTargets() {
        Card jace = new JaceIngeniousMindMage();
        harness.setLibrary(player1, List.of(jace));
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(jace);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grasping Current");
    }

    @Test
    void mayFailToFindJaceInLibrary() {
        Card jace = new JaceIngeniousMindMage();
        harness.setLibrary(player1, List.of(jace));
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(jace);
    }

    @Test
    void doesNotAutomaticallyTakeGraveyardCopyWhenLibraryAlsoHasOne() {
        Card graveyardJace = new JaceIngeniousMindMage();
        Card libraryJace = new JaceIngeniousMindMage();
        harness.setGraveyard(player1, List.of(graveyardJace));
        harness.setLibrary(player1, List.of(libraryJace));
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardJace);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryJace);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void allTargetsLeavingBattlefieldPreventsSearch() {
        harness.addToBattlefield(player2, new JacesSentinel());
        UUID targetId = harness.getPermanentId(player2, "Jace's Sentinel");
        Card jace = new JaceIngeniousMindMage();
        harness.setGraveyard(player1, List.of(jace));
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, List.of(targetId));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(jace);
        harness.assertNotInHand(player1, "Jace, Ingenious Mind-Mage");
        harness.assertInGraveyard(player1, "Grasping Current");
    }

    @Test
    void remainingLegalTargetIsBouncedAndSearchStillHappens() {
        harness.addToBattlefield(player1, new JacesSentinel());
        harness.addToBattlefield(player2, new JacesSentinel());
        UUID first = harness.getPermanentId(player1, "Jace's Sentinel");
        UUID second = harness.getPermanentId(player2, "Jace's Sentinel");
        Card jace = new JaceIngeniousMindMage();
        harness.setLibrary(player1, List.of(jace));
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, List.of(first, second));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Jace's Sentinel");
        harness.assertInHand(player1, "Jace's Sentinel");
        assertThat(gd.playerHands.get(player1.getId())).contains(jace);
    }

    @Test
    void cannotChooseMoreThanTwoCreatures() {
        harness.addToBattlefield(player2, new JacesSentinel());
        harness.addToBattlefield(player2, new JacesSentinel());
        harness.addToBattlefield(player2, new JacesSentinel());
        List<UUID> targets = gd.playerBattlefields.get(player2.getId()).stream()
                .map(p -> p.getId()).toList();
        harness.setHand(player1, List.of(new GraspingCurrent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }
}
